#!/usr/bin/env groovy

// #!/usr/local/bin/groovy




import groovy.sql.*
import org.inb.dbtask.RemoveMultAnsByDateDBTask
import org.inb.dbtask.RemoveQuestionnairePatientsTask

// import dbtask.RemoveMultAnsByDateDBTask

// STARTS GROOVY SCRIPT /////////////////////////////////////////////////////
// String dbUrl = "jdbc:postgresql://localhost:4321/appform"
// dbUrl = "jdbc:postgresql://padme:5432/appform"
// dbUrl = "jdbc:postgresql://ubio.bioinfo.cnio.es:5432/appform"
// dbUrl = "jdbc:postgresql://gredos:5432/appform"
// String dbUser = "gcomesana"
// String dbPass = "appform";

/*
println "Fixing repetad answers for ${dbUrl}..."
Sql theSqlConn = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')
*/



MIN_PATIENT_LENGTH = 9
PATIENTS_ARG = 'patients'
ANSWER_ARG = 'answers'

// ClassLoader.getSystemClassLoader().addUrl new File('dbtask').toURI().toURL()

/**
 * Perform the answers curation for those answers which belong to a single same
 * question. This is done by connecting to the database and performa a task to
 * remove the answers
 * @param String dbUrl
 * @param String dbUser
 * @param String dbPass
 * @param boolean simulation
 */
def curateAnswers(dbUrl, dbUser, dbPass, simulation) {

  println "Fixing repetad answers for ${dbUrl} for ${dbUser}:${dbPass}..."
  Sql theSqlConn = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

  if (theSqlConn != null) {

    String qry = '''select pga.codquestion, pga.codpat, pga.answer_number, pga.answer_order, pga.codanswer,
          ans.thevalue, ans.datecreated, ans.lastupdated
        from pat_gives_answer2ques pga, answer ans,
        (
        select pga.codquestion as idq, pga.codpat as codpat, pga.answer_order as ansord,
            pga.answer_number as ansnum, count (pga.codanswer) as anscount
        from pat_gives_answer2ques pga, answer a
        where pga.codanswer = a.idanswer
        group by 1, 2, 3, 4
        having count (pga.codanswer) > 1
        order by 2, 4, 3
        ) subq
        where pga.codquestion = subq.idq
          and pga.codpat = subq.codpat
          and pga.answer_number = subq.ansnum
          and pga.answer_order = subq.ansord
          and pga.codanswer = ans.idanswer
        order by 1, 2, 3, 4, 7, 8;'''

//  ResultSet rs = theSqlConn.executeQuery(qry);
/*
    if (args) {
      if (args.length == 1) {
        println("Arguments must be: 'sim 1' or 'sim 0' or nothing. Default is 'sim 1")
        return
      }
      else {
        simulation = args[1].compareToIgnoreCase("0") == 0 ? false : true;
      }
    }
*/
    if (simulation)
      println "Running database SIMULATION update..."
    else
      println "Running database LIVE update..."

    RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask(simulation)
//  RemoveMultAnsDBTask rmvdateTask = new RemoveMultAnsDBTask()

//  rmvdateTask.initSql(dbUrl, dbUser, dbPass)
    rmvdateTask.setQuery(qry);
    rmvdateTask.performTask(theSqlConn)
  }
} // EO fixAnswers




def deletePatients (dbHost, dbUser, dbPass, simulation, listCodsPatient) {

  def task = new RemoveQuestionnairePatientsTask (patients: listCodsPatient, sim: simulation)
  def localPort = '4321'
  localPort = '5432'
  localPort = dbHost == 'localhost'? '4321': '5432'
// println ("dbUrl: $localUrl; user: $localUser :: $localPass")
  Sql theSqlConn = Sql.newInstance("jdbc:postgresql://$dbHost:$localPort/appform", 'gcomesana', 'appform', 'org.postgresql.Driver')

println "About to perform patients deletion " + (simulation? "(SIMULATION)": "(live database update!!!)")
  task.performTask (theSqlConn)
  /*
  if (task.simulation)
    task.performTask (theSqlConn)
  else
    println "task is not in simulation mode: $task.simulation"
  */
}







/**
 * Read the file for remove patients, line per line, and adds the string into
 * the list of patients. PAY ATTENTION to the lines which have to be of the shape
 * <patient_code>,<questionnaire name|questionnaire id>,<study-name|study-code>
 */
def getPatientCodesFromFile (filename) {

  fileCodes = new File (filename)
  listPatientCodes = []
  charstoRead = 1024
  offset = 0
  currentCode = ''

  fileCodes.withReader { reader ->
    while ((currentCode = reader.readLine()) != null)
      listPatientCodes << currentCode

  } // EO reader closure

  listPatientCodes
}






// START groovy script ////////////////////////////////////////////////
dbUrl = "jdbc:postgresql://gredos:5432/appform"
dbuser = 'gcomesana'
dbpass = 'appform'
dbhost = 'gredos'
// simOpt = !opts.sim || opts.sim == '1'
/*
simOpt = true
String filename = 'test/patients.txt'
listPatientCodes = getPatientCodesFromFile(filename)
deletePatients (dbhost, dbuser, dbpass, simOpt, listPatientCodes)
*/

// Start script

myCliHelp = "fixtool.[bat|sh] -what <patients|answers> [-codes <file>] [-sim <0|1>] "
// myCliHelp += "-dbhost <host> -dbname <name> -dbuser <user> -dbpasswd <passwd>"
// myCliHelp += " [-dbport <port>]"

def myCli = new CliBuilder(usage: myCliHelp)
myCli.what(args:1, argName: 'what', "what fix (patients, for deletion, or repeated answers)")
myCli.sim(args: 1, argName: 'sim', "if perform a simulated operation or really modify database contents (default simulation on, 1)")
myCli.help(args: 1, argName: 'help', "print all this")
myCli.codes(args: 1, argName: 'file', "a text file with the list of patients an questionnaires to delete")
myCli.dbhost(args: 1, argName: 'dbhost', "name of database server")
// myCli.dbuser(args: 1, argName: 'dbuser', 'database user')
// myCli.dbname(args: 1, argName: 'dbname', 'the name of the database')
// myCli.dbpasswd(args: 1, argName: 'dbpasswd', 'password for the user')
// myCli.dbport(args: 1, argName: 'dbport', 'database server port (default to 4321)')

def opts = null
if (!args) {
  args = ['-what',  'answers', '-dbhost',  'localhost', '-sim', '1']
  opts = myCli.parse(args)
}
else
  opts = myCli.parse(args)


/* what and codes parameters check
if (!opts.what) {
  println "What kind of curation is to be perfomed must be provided\n"
  myCli.usage()
  return
}
else { // what parameter exists but it is contextual...
  if (opts.what != PATIENTS_ARG && opts.what != ANSWER_ARG) {
    println "Wrong value for parameter 'what': must be one of patiens, answers\n"
    myCli.usage()
    return
  }

  if (opts.what == PATIENTS_ARG && !opts.codes) {
    println "Parameter codes must be provided along with '-what patients'\n"
    myCli.usage()
    return
  }
}


if (!opts || args.size() == 0) {
  println "No options provided"
  myCli.usage()
  return
}


if (opts.help) {
  println "usage: $myCliHelp"
  myCli.usage()
  return
}


* database parameters check
if (!(opts.dbhost && opts.dbname && opts.dbpasswd && opts.dbuser)) {
  println "Database parameters dbhost, dbname and dbpasswd are mandatory\n"
  myCli.usage()
  return
}


dbPort = opts.dbport? opts.dbport: '5432'
// patch for running it on development machine and local database (different port)
if (opts.dbhost.compareTo('localhost') == 0) {
  dbPort = '4231'
}
*/

// dbUrl = "jdbc:postgresql://$opts.dbhost:$dbPort/appform"
// dbUrl = "jdbc:postgresql://gredos:5432/appform"
dbUrl = "jdbc:postgresql://localhost:4321/appform170613"
dbuser = 'gcomesana'
dbpass = 'appform'
simOpt = opts.sim == null || opts.sim == '1' // TODO here is a problem :-S
simOpt = true
println "** simulation: ${!opts.sim} || ${opts.sim == '1'} **"

what = ANSWER_ARG

if (what == ANSWER_ARG)
  curateAnswers (dbUrl, dbuser, dbpass, simOpt)

else {
  if (!opts.codes) {
    println "A comma-separated file with patient codes to delete must be provided\n"
    myCli.usage()                           
    return
  }

  String filename = opts.codes
  listPatientCodes = getPatientCodesFromFile(filename)
  deletePatients (opts.dbhost, dbuser, dbpass, simOpt, listPatientCodes)
}

