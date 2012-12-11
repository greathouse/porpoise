@GrabConfig(systemClassLoader=true)
@Grapes([
	@Grab(group='com.h2database', module='h2', version='1.3.170'),
	@Grab(group='net.sourceforge.jtds', module='jtds', version='1.2.4')
])

import groovy.sql.*
import java.security.MessageDigest

def cli = new CliBuilder(usage: "${this.class.simpleName}.groovy")
cli.with {
	F (longOpt: 'force-removals', 'Indicates that removals SHOULD run. Otherwise, script removals are only noted.')
	D (longOpt: 'dry-run', 'Outputs log information only. Does not run sql scripts')
	d (longOpt: 'dir', args: 1, required: false, 'Path to SQL script directory (Optional. Defaults to startup-directory)')
	p (longOpt: 'database-password', args: 1, required: false, 'Database Password (Optional)')
	U (longOpt: 'url', args: 1, required: true, 'JDBC URL definition')
	u (longOpt: 'database-user', args: 1, required: false, 'Database user (Optional)')
}


def opts = cli.parse(args)
if (!opts) {
	System.exit(1)
}

dryRun = opts.D
forceRemovals = opts.F

def dbUrl = opts.U
def dbUser = opts.u ?: null
def dbPassword = opts.p ?: null
def scriptDirectoryPath = opts.d ?: System.getProperty('user.dir')
scriptDirectory = new File(scriptDirectoryPath)

sql = Sql.newInstance(dbUrl, dbUser, dbPassword, 'net.sourceforge.jtds.jdbc.Driver')
databaseProduct = sql.connection.metaData.databaseProductName
println "Connected to a \"${databaseProduct}\" database"

scriptLogLines = []

checkAndCreateLogTable()

def scripts = determineScriptsToRun()
if (!dryRun) {
	scripts.each { scriptMetadata ->
		executeScript(scriptMetadata)
	}
}

println '\n---------------------------------------'
def ups = scripts.findAll{it.needsUp}
if (ups) {
	println 'Appling the following...'
	ups.each {
		println "\t${it.changeset}/${it.script}"
	}
}

def downs = scripts.findAll{it.needsDown}
if (downs) {
	if (forceRemovals) { println 'Removed the following:' }
	else { println 'Scripts marked for removal, use "-F" option to force removals'}
	downs.each {
		println "\t${it.changeset}/${it.script}"
	}
}

def changed = scripts.findAll{it.hasChanged}
if (changed) {
	println 'WARNING: THE FOLLOWING HAVE CHANGED'
	changed.each {
		println "\t${it.changeset}/${it.script}"
	}
}


println 'Done!'

/////////////////////////////////////

def checkAndCreateLogTable() {
	def tables = sql.connection.metaData.getTables(null, null, "PORP_SCHEMA_LOG", null)
	if (tables.next()) { return }
	
	print "Preparing Porpoise database tables....."
	executeSql("""create table PORP_SCHEMA_LOG (
		ID varchar(50),
		CHANGESET varchar(500),
		SCRIPT_NAME varchar(500),
		MD5 varchar(32),
		DATE_APPLIED ${timestampType()},
		UP_SCRIPT ${largeObjectType()},
		DOWN_SCRIPT ${largeObjectType()}
	);
	""".toString())
	println "SUCCESS"
}

def largeObjectType(def dbMetaData) {
	switch (databaseProduct.toLowerCase()) {
		case "microsoft sql server":	return "nvarchar(max)"
		case "h2":
		default: return "clob"
	}
}

def timestampType(def dbMetaData) {
	switch (databaseProduct.toLowerCase()) {
		case "microsoft sql server":	return "datetime"
		case "h2":
		default: return "timestamp"
	}
}

def determineScriptsToRun() {
	def scripts = []
	sql.eachRow("select * from porp_schema_log") { appliedScript ->
		scripts.add([
			changeset:appliedScript.changeset, 
			script:appliedScript.script_name,
			up:appliedScript.up_script.asciiStream.text,
			down:appliedScript.down_script.asciiStream.text,
			md5:appliedScript.md5,
			needsUp:false,
			needsDown:true, //initially expect not to be present. Will be checked later
			hasChanged:false
		])
	}
		
	scriptDirectory.eachDir { dir ->
		def changeset = dir.name
		dir.eachFile { file ->
			def script = file.name
			def upAndDown = file.text.split('(?i)--down')
			def up = upAndDown[0].trim()
			def down = (upAndDown.size() == 2) ? upAndDown[1].trim() : ""
			def md5 = generateMd5(file)
			
			def applied = scripts.find { it.changeset == changeset && it.script == script }
			if (applied == null) {
				scripts.add([
					changeset:changeset,
					script:script,
					up:up,
					down:down,
					md5:md5,
					needsUp:true,
					needsDown:false,
					hasChanged:false
				])
				return
			}
			applied.needsDown = false
			if(applied.md5 != md5) { applied.hasChanged = true }
		}
	}
	return scripts
}

def executeScript(scriptMetadata) {
	if (scriptMetadata.needsUp) {
		scriptMetadata.up.split(";").each {
			executeSql(it)
		}
		executeSql("insert into porp_schema_log (id, changeset, script_name, md5, date_applied, up_script, down_script) values (${UUID.randomUUID().toString()}, ${scriptMetadata.changeset}, ${scriptMetadata.script}, ${scriptMetadata.md5}, ${new java.sql.Timestamp(new Date().time)}, ${scriptMetadata.up}, ${scriptMetadata.down});")
		return
	}
	
	if (forceRemovals && scriptMetadata.needsDown) {
		scriptMetadata.down.split(";").each {
			executeSql(it)
		}
		executeSql("delete from porp_schema_log where md5 = ${scriptMetadata.md5}")
		return
	}
}

def executeSql(def stmt) {
	scriptLogLines << stmt
	
	if (dryRun) return
	
	sql.execute(stmt)
}

def generateMd5(final file) {
   MessageDigest digest = MessageDigest.getInstance("MD5")
   file.withInputStream(){is->
   byte[] buffer = new byte[8192]
   int read = 0
      while( (read = is.read(buffer)) > 0) {
             digest.update(buffer, 0, read);
         }
     }
   byte[] md5sum = digest.digest()
   BigInteger bigInt = new BigInteger(1, md5sum)
   return bigInt.toString(16)
}