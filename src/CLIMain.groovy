#!/usr/bin/env groovy

import groovy.sql.Sql
import groovy.util.CliBuilder
import org.inb.dbtask.FixingTasksHub


/**
 * Command Line Interface for curation script. So far, the paramters are:
 * -s, --server
 * -p, --port
 * -u, --user
 * -x, --pasword
 * -h, --help
 * -l, --live (for simulation or live update)
 * -csc, --changesubjectscodes <file>
 * -rs, --removesubjects <file>
 * -ans, --answerscuration
 * -rm, --removeinterviews
 */
def cli = new CliBuilder(usage:'CLIMain.groovy')
cli.h(longOpt:'help', 'print this message')
cli.s(longOpt:'server',
  args:1,
  argName:'server',
  'database server <default: localhost>'
)
cli.p(longOpt: 'port',
  args: 1,
  argName: 'port',
  'database port <default: 5432>'
)
cli.u(longOpt:'user',
  args:1,
  argName: 'database user',
  'database user'
)
cli.x(longOpt: 'password',
  args: 1,
  argName: 'password',
  'database password for user'
)
cli.l(longOpt: 'live',
  args: 1,
  argName: 'simulation',
  'live or simulation update: 1 for live update; 0 for simulation (default)'
)
cli.csc(longOpt:'changesubjectscodes',
  args: 1,
  argName: 'file',
  'change the code for subjects. The argument must be a file, each line like old_subject_code:new_subject_code'
)
cli.rs(longOpt: 'removesubjects',
  args: 1,
  argName: 'file',
  'remove subjects from database. The argument must be a file, each line a subject to delete'
)
cli.ans(longOpt: 'answercuration',
  'performs a database cleansing by removing multiple answers for a question')
// cli.rm(longOpt: 'removeinterviews', 'Remove interviews')

def options = cli.parse(args)
def connOptionsProvided = options && (options.s && options.u && options.x)
def dbUrl = ""
if (connOptionsProvided) {
  def port = options.p ? options.p: "5432"
  dbUrl = "jdbc:postgresql://${options.s}:${port}/appform"
}
def liveUpdate = false

if (!options) {
  cli.usage()
  System.exit(1)
}

if (options && options.h)
  cli.usage()

else if (connOptionsProvided && options.csc) {
  println("server: ${options.s}; port: ${options.p}; usr: ${options.u}; pass: ${options.x}")
  println("changeSubjectsCode ${options.csc}")
  FixingTasksHub fs = new FixingTasksHub(dbUrl, options.u, options.x)
  Map mapCodes = fs.getSubjectCodesMapFromFile(options.csc)
  liveUpdate = (options.l && options.l.equals("1"))
  fs.changeSubjecsCode(options.s, liveUpdate, mapCodes)
}
else if (connOptionsProvided && options.rs) {
  println("server: ${options.s}; port: ${options.p}; usr: ${options.u}; pass: ${options.x}")
  println("removesubjects ${options.csc}")
}
else if (connOptionsProvided && options.ans) {
  println("server: ${options.s}; port: ${options.p}; usr: ${options.u}; pass: ${options.x}")
  println("answercuration")
}
else if (connOptionsProvided) {
  def port = options.s ? options.p: "5432"
// dbUrl = "jdbc:postgresql://ubio.bioinfo.cnio.es:5432/appform"
// dbUrl = "jdbc:postgresql://gredos:5432/appform"
  String dbUser = options.u
  String dbPass = options.x

  Sql theSqlConn = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')
  if (theSqlConn) {
    println "Connection successful for ${dbUrl}"
    theSqlConn.close()
  }
  else
    println("Unable to get a connection. Please, review the parameters.")
}
else
 cli.usage()