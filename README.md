Porpoise
========

Porpoise is a database schema migration tool. It is ran via the command line. Scripting Porpoise is a breeze, 
so it can be integrated into most workflows.

Command Line Parameters
-----------------------

<pre>
usage: Porpoise.groovy
 -D,--dry-run                   Outputs log information only. Does not run sql scripts
 -d,--dir <arg>                 Path to SQL script directory (Optional. Defaults to startup-directory)
 -F,--force-removals            Indicates that removals SHOULD run. Otherwise, script removals are only noted.
 -p,--database-password <arg>   Database Password (Optional)
 -U,--url <arg>                 JDBC URL definition
 -u,--database-user <arg>       Database user (Optional)
</pre>

Overview
--------

Porpoise will find all sql files in the specified directory and run any scripts that have not been previously applied to the database.
Porpoise works with changesets. The -d argument indicates the root directory for the SQL scripts. Porpoise will
traverse the one-level of subdirectories looking for sql files to run. This strategy is useful for seperating feature
and bug migrations. The SQL files in the subdirectories will be ran in order.

Example SQL Script Directory
----------------------------

<pre>
/SQL_Scripts
   /ChangeSet1
      /001-Create-Person-Table.sql
      /002-Populate-PersonTable.sql
   /ChangeSet2
      /001-Create-Jobs-Table.sql
      /002-Populate-Jobs.sql
</pre>

The SQL Scripts should contain both the "up" and "down" sql statements. The sections are seperated by a "--down" comment.
The SQL statements should be deliminated with semicolons (;).
```sql
create table person (id varchar(50));
--down
drop table person;
```

Output
------
Porpoise will output the status at the end of the migration. It will indicate scripts in each of the following states:
* Applied
* Removed (if -F flag is passed)
* Changed since being applied (<em>see below</em>)
* Failed (<em>see below</em>)
* Require Applying (<em>see below</em>)
* Require Removal (<em>see below</em>)

Changed Scripts
---------------
Porpoise generates and maintains an MD5 signature for each script it applies. If the script has changed since it was
applied, Porpoise will NOT apply or remove that script. It merely indicates that the script has changed. It is up to 
you to decide how to handle it.

Failed Scripts
--------------
If a script fails to execute, Porpoise will abort the process. The failed script may be in a half-applied state.
You will be responsible for backing out any half-applied scripts. Sorry, such is life.

Require Applying
----------------
A script will end up in the "require applying" if a prior script failed. Since Porpoise aborted the remaining scripts.

Require Removal
---------------
A script may be in the "require removal" state if the -F flag was not set. Scripts may also end up in this state
if a prior script failed.

Compatibility
-------------
Porpoise has been tested with the following databases:
* Microsoft SQL Server (http://www.microsoft.com/sqlserver)
* H2 (http://www.h2database.com)

But really, it is not difficult to add more database engines. Porpoise looks for a table called "PORP_SCHEMA_LOG". 
If this table does not exist, it attempts to create it. Database engines use different datatypes for clobs,
timestamps, etc. If your database engine is not supported, please review the script and make a pull request. Otherwise,
you might be able to create the table manually. Below is a sample create table statement. Modify the datatypes to match
your database engines preference.
```sql
CREATE TABLE PORP_SCHEMA_LOG(
 ID varchar(50) NULL,
	CHANGESET varchar(500) NULL,
	SCRIPT_NAME varchar(500) NULL,
	MD5 varchar(32) NULL,
	DATE_APPLIED datetime NULL,
	UP_SCRIPT nvarchar(max) NULL,
	DOWN_SCRIPT nvarchar(max) NULL
)
```
