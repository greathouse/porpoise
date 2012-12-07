@GrabConfig(systemClassLoader=true)
@Grapes(
	@Grab(group='com.h2database', module='h2', version='1.3.170')
)

import groovy.sql.*
import java.security.MessageDigest

def dbUrl = "jdbc:h2:tcp://localhost/~/test"
sql = Sql.newInstance(dbUrl, 'sa', '', "org.h2.Driver")

scriptDirectory = new File(/C:\Users\kofspades\projects\porpoise\SampleScripts/)
dryRun = false

checkAndCreateLogTable()

def scripts = determineScriptsToRun()
if (!dryRun) {
	scripts.each { scriptMetadata ->	
		executeScript(scriptMetadata)
	}
}

def ups = scripts.findAll{it.needsUp}
if (ups) {
	println 'Appling the following...'
	ups.each {
		println "\t${it.changeset}/${it.script}"
	}
}

def downs = scripts.findAll{it.needsDown}
if (downs) {
	println 'Removed the following:'
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
	sql.execute("""create table PORP_SCHEMA_LOG (
		ID varchar(50),
		CHANGESET varchar(500),
		SCRIPT_NAME varchar(500),
		MD5 varchar(32),
		DATE_APPLIED timestamp,
		UP_SCRIPT clob,
		DOWN_SCRIPT clob
	);
	""")
	println "SUCCESS"
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
			sql.execute(it)
		}
		sql.execute("insert into porp_schema_log (id, changeset, script_name, md5, date_applied, up_script, down_script) values (${UUID.randomUUID().toString()}, ${scriptMetadata.changeset}, ${scriptMetadata.script}, ${scriptMetadata.md5}, ${new Date()}, ${scriptMetadata.up}, ${scriptMetadata.down});")
		return
	}
	
	if (scriptMetadata.needsDown) {
		scriptMetadata.down.split(";").each {
			sql.execute(it)
		}
		sql.execute("delete from porp_schema_log where md5 = ${scriptMetadata.md5}")
		return
	}
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