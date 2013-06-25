package dbtask

import groovy.sql.Sql

/**
 * Created with IntelliJ IDEA.
 * User: bioinfo
 * Date: 17/06/13
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
class FixTasks {


	def noDeletedPatients


  public FixTasks () {
    super();

	  this.noDeletedPatients = []
  }

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
    def patientsDel = task.performTask (theSqlConn)

	  this.noDeletedPatients = task.getSubjectsWithSamples()

	  patientsDel
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

  /**
   * Try to delete all users from the database, including its performances, answers
   * included.
   * To do it, as a prerequisite, the patient can't have related samples. If so, no
   * deletion has to be performed
   * @param listSubjects a List with the subject ids
   */
  def deleteUsers (listSubjects) {
    // get users without samples
    // get answers for each user and delete the answers first
    // get performances for each user and delete (no need, are deleted with user)
    // delete user itself
    // the only point is to delete the answers first






  }


}
