package org.inb.dbtask

import groovy.sql.Sql

/**
 * Interface class to call the actual task classes
 */
class FixingTasksHub {

	ArrayList<String> noDeletedPatients

	private def dbUrl
	private def dbUser
	private def dbPasswd


	public FixingTasksHub() {
		super()

		this.noDeletedPatients = []
	}


	public FixingTasksHub(dbUrl, dbUsr, dbPass) {
		this()

		this.dbUrl = dbUrl
		this.dbUser = dbUsr
		this.dbPasswd = dbPass
		println("DBConn params: ${this.dbUrl}, user: ${this.dbUser}:${this.dbPasswd}")
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

		// println "Fixing repetad answers for ${dbUrl} for ${dbUser}:${dbPass}..."
		// Sql theSqlConn = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')
		Sql theSqlConn = Sql.newInstance(this.dbUrl, this.dbUser, this.dbPasswd, 'org.postgresql.Driver')

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
	 * @param listCodsPatient , the list of patients code to delete
	 * @return
	 */
	def deletePatients(dbHost, dbUser, dbPass, simulation, listCodsPatient) {

		def task = new RemovePatientsTask(patients: listCodsPatient, sim: simulation)
		def localPort = '4321'
		localPort = '5432'
    localPort = dbHost == 'localhost' ? '5432' : '5432'
    def host = dbHost == null? 'localhost': dbHost
    def user = dbUser == null? this.dbUser: dbUser
    def passwd = dbPass == null? this.dbPasswd: dbPass
    def url = this.dbUrl == null? "jdbc:postgresql://$host:$localPort/appform": this.dbUrl

    // println("FixTasks deleteInterviews -> dbUrl: ${url}; ${user}:${passwd}")
    Sql theSqlConn = Sql.newInstance(url, user, passwd, 'org.postgresql.Driver')

		println "About to perform patients deletion " + (simulation ? "(SIMULATION)" : "(live database update!!!)")
		this.noDeletedPatients = task.getSubjectsWithSamples()

		Integer patientsDel = task.performTask(theSqlConn, {})
		def jsonOut = [
		  rows_affected: patientsDel,
			pats_with_samples: task.getPatientsWithSamples(),
			// pats_unaffected: task.getUnaffectedPats()
			pats_affected: task.getListOfDeletions()
		]
		jsonOut

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
	def deleteInterviews(dbHost, dbUser, dbPass, simulation, mapInterviews) {

		def localPort = '4321'
		localPort = '5432'
		localPort = dbHost == 'localhost' ? '5432' : '5432'
    def host = dbHost == null? 'localhost': dbHost
    def user = dbUser == null? this.dbUser: dbUser
    def passwd = dbPass == null? this.dbPasswd: dbPass
    def url = this.dbUrl == null? "jdbc:postgresql://$host:$localPort/appform": this.dbUrl

		// println("FixTasks deleteInterviews -> dbUrl: ${url}; ${user}:${passwd}")
		Sql theSqlConn = Sql.newInstance(url, user, passwd, 'org.postgresql.Driver')

		def task = new RemoveInterviewsTask(interviews: mapInterviews, sim: simulation)
		def totalRowsDeleted = task.performTask(theSqlConn, {})
		def patsWithSamples = task.getPatientsWithSamples()
		def interviewsDel = task.getInterviewsDeleted()
    def lastInterviews = task.getLastInterviews() as Set

		def jsonOut = [
			rows_affected: totalRowsDeleted, // rows_affected -> 2008
			pats_with_samples: patsWithSamples, // pats_with_samples -> 157001002:[samples]
			interviews_deleted: interviewsDel, // interviews_deleted -> [[157..., QES...],...]
      last_interviews: lastInterviews // non-repeating list of pairs pat -> true/false
		]
    // println("jsonOut: $jsonOut")
		jsonOut
	}



	/**
	 * Change the code of the subjects. This can be meant as an error from the monitor.
	 * To perform the change:
	 * - check whether or not the subject has samples
	 * - if ready to change, patient(codpatient, codprj, codhosp, cod_type_subject, codpat)
	 * fields have to be updated.
	 * No need to change any FK's as the subjecst does not have any.
	 * @param dbHost the database host
   * @param dbUser the database user
   * @param dbPass password for the database user
   * @param simulation if simulation or live update
	 * @param mapCodes, a map such that oldCode -> newCode
	 */
	def changeSubjecsCode (dbHost, simulation, mapCodes) {
		def localPort = '4321'
		localPort = '5432'
		localPort = dbHost == 'localhost' ? '4321' : '5432'

		println("FixTasks changeSubjecsCode -> dbUrl: ${this.dbUrl}; ${this.dbUser}:${this.dbPasswd}")
		Sql theSqlConn = Sql.newInstance(this.dbUrl, this.dbUser, this.dbPasswd, 'org.postgresql.Driver')

		def task = new ChangeSubjectsCodeTask(patCodes: mapCodes, sim: simulation)
		def totalRowsDeleted = task.performTask(theSqlConn, {})
		def patsWithSamples = task.getPatientsWithSamples()
		def patientsUnchanged = task.getPatientsUnchanged()
    def patientsNonExistent = task.getPatientsNonexistent()

		def jsonOut = [
			rows_affected: totalRowsDeleted,
			pats_with_samples: patsWithSamples,
			patients_unchanged: patientsUnchanged,
      patients_nonexistent: patientsNonExistent
		]
		jsonOut
	}

  /**
   * Read a map of subjects code and subjects NEW codes (to change) from file
   * The format of each line of the file must be:
   * <old_subject_code>:<new_subject_code>
   * @param filename the name of the file
   * @return a Map structure with entries for old and new subjects
   */
  Map getSubjectCodesMapFromFile (filename) {
    def fileCodes = new File(filename)
    def listPatientCodes = [:]

    fileCodes.eachLine {
      def codes = it.split(":")
      if (codes.length == 0)
        codes = it.split("=")

      if (codes.length > 1) // check blank lines
        listPatientCodes.put(codes[0], codes[1])
    }
    listPatientCodes
  }


  /**
   * Parse the subject codes file. It is intended to get a java.io.InputStream
   * as processed by a upload file servlet
   * @param fis a java.io.InputStream
   * @return a Map with the old_code:new_code; null if the file (input stream) is malformed
   */
  Map parseSubjectCodesFile(fis) {
    if (!fis)
      return null;

    def listPatientCodes = [:]
    fis.eachLine {
      def codes = it.split(":")
      if (codes.length == 0)
        codes = it.split("=")

      if (codes.length > 1) // check blank lines
        listPatientCodes.put(codes[0], codes[1])
    }
    listPatientCodes
  }


  /**
   * Gets a list of subjects from a input stream. It is intended to be used from a
   * upload file servlet. The file must contain a subject per line
   * @param fis a java.io.InputStream with a subject code per line
   * @return a List of the retrieved subject codes or null if the content is malformed
   */
  List getSubjectsList (fis) {
    def listPatientCodes = []
    if (!fis)
      return null;

    ((InputStream)fis).eachLine {
      listPatientCodes << it
    }
    return listPatientCodes
  }



	/**
	 * Read the file for remove patients, line per line, and adds the string into
	 * the list of patients.
	 * PAY ATTENTION to the lines which have to be of the shape
	 * <patient_code>,<questionnaire name|questionnaire id>,<study-name|study-code>
	 */
	def getSubjectListFromFile(filename) {

		def fileCodes = new File(filename)
		def listPatientCodes = []
		def charstoRead = 1024
		def offset = 0
		def currentCode = ''

		fileCodes.withReader { reader ->
			while ((currentCode = reader.readLine()) != null)
				listPatientCodes << currentCode

		} // EO reader closure

		listPatientCodes
	}


	def ping () {
		return '{"ping":"success"}'
	}

}