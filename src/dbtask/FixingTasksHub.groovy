package dbtask

import groovy.sql.Sql

/**
 * Interface class to call the actual task classes
 */
class FixingTasksHub {

     ArrayList<String> noDeletedPatients

     private def dbUrl
     private def dbUser
     private def dbPasswd


  public FixingTasksHub () {
    super()

       this.noDeletedPatients = []
  }


     public FixingTasksHub (dbUrl, dbUsr, dbPass) {
          this()

          this.dbUrl = dbUrl
          this.dbUser = dbUsr
          this.dbPasswd = dbPass
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
      rmvdateTask.performTask(theSqlConn, {})
    }
  } // EO fixAnswers


     /**
      * Delete patients without samples associated.
      * To do it, as a prerequisite, the patient can't have related samples. If so, no
      * deletion has to be performed
      * @param dbHost
      * @param dbUser
      * @param dbPass
      * @param simulation
      * @param listCodsPatient, the list of patients code to delete
      * @return
      */
  Integer deletePatients (dbHost, dbUser, dbPass, simulation, listCodsPatient) {

    def task = new RemovePatientsTask (patients: listCodsPatient, sim: simulation)
    def localPort = '4321'
    localPort = '5432'
    localPort = dbHost == 'localhost'? '4321': '5432'
    println ("FixTasks deletePatients -> dbUrl: jdbc:postgresql://$dbHost:$localPort/appform;")
    Sql theSqlConn = Sql.newInstance("jdbc:postgresql://$dbHost:$localPort/appform", dbUser, dbPass, 'org.postgresql.Driver')

    println "About to perform patients deletion " + (simulation? "(SIMULATION)": "(live database update!!!)")
       this.noDeletedPatients = task.getSubjectsWithSamples()

       Integer patientsDel = task.performTask(theSqlConn, {})
       patientsDel
    /*
    if (task.simulation)
      task.performTask (theSqlConn)
    else
      println "task is not in simulation mode: $task.simulation"
    */
  }




     /**
      * Delete interviews. As per request, the task for removing interviews has
      * the restriction based on samples for patients if interview to remove is
      * QES
      * @param dbHost the database host
      * @param dbUser the database user
      * @param dbPass password for the database user
      * @param simulation if simulation or live update
      * @param mapInterviews a map list where the keys are the patients code and the values
      * the interviews to delete...
      */
     def deleteInterviews (dbHost, dbUser, dbPass, simulation, mapInterviews) {

          def localPort = '4321'
    localPort = '5432'
    localPort = dbHost == 'localhost'? '4321': '5432'
    // println ("FixTasks deleteInterviews -> dbUrl: $localUrl; user: $localUser :: $localPass")
          println ("FixTasks deleteInterviews -> dbUrl: jdbc:postgresql://$dbHost:$localPort/appform;")
    Sql theSqlConn = Sql.newInstance("jdbc:postgresql://$dbHost:$localPort/appform",
             dbUser, dbPass, 'org.postgresql.Driver')

          def task = new RemoveInterviewsTask (interviews: mapInterviews, sim: true)
          def totalRowsDeleted = task.performTask(theSqlConn, {})
          def patsWithSamples = task.getPatientsWithSamples()
          def interviewsDel = task.getInterviewsDeleted()

          def jsonOut = [
            rows_affected: totalRowsDeleted,
               pats_with_samples: patsWithSamples,
               interviews_deleted: interviewsDel
          ]
          jsonOut
     }







  /**
   * Read the file for remove patients, line per line, and adds the string into
   * the list of patients.
   * PAY ATTENTION to the lines which have to be of the shape
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

}