porpoise
========

Database Schema Patching Script

<pre>
Usage
usage: Porpoise.groovy
 -D,--dry-run                   Outputs log information only. Does not run sql scripts
 -d,--dir <arg>                 Path to SQL script directory (Optional. Defaults to startup-directory)
 -F,--force-removals            Indicates that removals SHOULD run. Otherwise, script removals are only noted.
 -p,--database-password <arg>   Database Password (Optional)
 -U,--url <arg>                 JDBC URL definition
 -u,--database-user <arg>       Database user (Optional)
 </pre>