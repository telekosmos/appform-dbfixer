package org.inb.util

import groovy.sql.Sql

/**
 * Created with IntelliJ IDEA.
 * User: bioinfo
 * Date: 18/06/13
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public class DBQuery {

	private String QES_SPAIN = "QES_Español"

  private String dbUrl
  private String dbUsr
  private String dbPasswd
  private Sql theSqlConn

  public static String DUMMY_PAT = "15769696969"

  def selPatSamples = """
    select p.idpat, p.codpatient
    from patient p
    where p.codpatient like '?__'
    	and p.codpatient <> '${DBQuery.DUMMY_PAT}'
    order by 2;
  """



	def selPatientPerf = """select i.idinterview, pf.idperformance, p.idpat
		from interview i, patient p, performance pf
		where i.name = ? -- interview name
		  and pf.codinterview = i.idinterview
		  and p.codpatient = ? -- patient code
		  and p.codpatient <> '${DBQuery.DUMMY_PAT}'
		  and pf.codpat = p.idpat;
	"""


	// params for selAnswersQry are codpatient, codproject and questionnaire name
  def selAnswersQryQuestionnaire = """select a.idanswer, pga.idp_a_q, a.thevalue, p.idpat, p.codpatient
      from patient p, pat_gives_answer2ques pga, answer a, question q, item it
      where p.codpatient = ?
        -- and p.idpat = xxx
        and p.codpatient <> '${DBQuery.DUMMY_PAT}'
        and p.idpat = pga.codpat
        and pga.codanswer = a.idanswer
        and pga.codquestion = q.idquestion
        and q.idquestion = it.iditem
        and q.idquestion in
            (select idquestion
             from question q, item i, section s, interview iv, project p
             where 1 = 1 -- s.codinterview = it.idinterview
               and upper(iv.name) = upper(?)
               and p.project_code = ?
               and iv.codprj = p.idprj
               and s.codinterview = iv.idinterview
               and i.idsection = s.idsection
               and q.idquestion = i.iditem );
  """


	def selAnswersQry = """select a.idanswer, a.thevalue, p.idpat, p.codpatient
    from patient p, pat_gives_answer2ques pga, answer a, question q, item it
    where p.codpatient = ?
    	and p.codpatient = '${DBQuery.DUMMY_PAT}'
      and p.idpat = pga.codpat
      and pga.codanswer = a.idanswer
      and pga.codquestion = q.idquestion
      and q.idquestion = it.iditem
      and q.idquestion in
          (select idquestion
           from question q, item i, section s, interview iv, project p
           where 1 = 1 -- s.codinterview = it.idinterview
             and p.project_code = ?
             and iv.codprj = p.idprj
             and s.codinterview = iv.idinterview
             and i.idsection = s.idsection
             and q.idquestion = i.iditem );
  """

  def selNumIntrvs4Pat = """select count(i.idinterview) as counter
    from patient p, interview i, performance pf
    where p.codpatient = ?
    	and p.codpatient = '${DBQuery.DUMMY_PAT}'
      and i.idinterview = pf.codinterview
      and pf.codpat = p.idpat;
  """


	def updPatCode = """update patient
		set codpatient = '?', -- '157072017',
		   codprj = '?', -- '157',
		   codhosp = '?', -- '07',
		   cod_type_subject = '?', -- '2',
		   codpat = '?' -- '017'
		where codpatient = '?' -- '157072005'
		  and codpatient <> '${DBQuery.DUMMY_PAT}';
	"""

	def deletePerf = """delete
		from performance
		where idperformance = ?;
	"""


	def deleteAnswers = """delete
		from answer
		where idanswer in (?);
	"""


	def deletePGQAs = """delete
		from pat_gives_answer2ques
		where idp_a_q in (?);
	"""


	def selQuestionnaires = """
	select idinterview, name
	from interview i
	where i.name = ?;
	"""


	def selQuestionnaireFromId = """
		select idinteriew, name
		from interview i
		where i.idinterview = ?;
	"""



  public DBQuery () {
    super();
  }


  public DBQuery (dbUrl, dbUser, dbPassword) {
    this.dbPasswd = dbPassword
    this.dbUrl = dbUrl
    this.dbUsr = dbUser

	  this.theSqlConn = Sql.newInstance(this.dbUrl, this.dbUsr, this.dbPasswd, 'org.postgresql.Driver')
  }


  public DBQuery (dbConn) {
    this.theSqlConn = dbConn
  }


  def Sql getDbConn () {
    theSqlConn = Sql.newInstance(this.dbUrl, this.dbUsr, this.dbPasswd, 'org.postgresql.Driver')
    theSqlConn
  }


	Sql setDbConn (dbConn) {
		this.theSqlConn = dbConn
		this.theSqlConn
	}


  def closeDbConn () {
    theSqlConn.close();
  }


  /**
   * Gets the codes for the samples related to this patient
   * @param String codPatient the patient code
   * @return a list with the codes for the samples, if any found
   */
  LinkedHashMap getSamples4Patient (codPatient) {
    def sampleCodes = []

    if (theSqlConn == null)
      this.getDbConn()

    def selSamplesQry = this.selPatSamples.replaceFirst('\\?', codPatient)
    // def rows = []
    def res = [:]

    def rows = theSqlConn.rows(selSamplesQry)
    rows.each { row ->
      res[row.idpat] = row.codpatient
    }
    res
  }



	List getAnswers (codPatient, codProject) {
		if (theSqlConn == null)
			this.getDbConn()

		def rows = theSqlConn.rows(selAnswersQry, [codPatient, codProject])

		rows
	}


	def getAnswers4UserIntrv (codPatient, codProject, questionnaireName) {
		if (theSqlConn == null)
			this.getDbConn()

		def params = [codPatient, questionnaireName, codProject]
		def myQry = this.selAnswersQryQuestionnaire.replaceFirst("\\?", "'$codPatient'")
/*
		if (questionnaireName.equals(QES_SPAIN)) {
			myQry = myQry.replaceFirst("\\?", "'$QES_SPAIN'")
			println("## equals: $questionnaireName and $QES_SPAIN")
		}
		else {
			myQry = myQry.replaceFirst("\\?", "'$questionnaireName'")
			println("## different: $questionnaireName and $QES_SPAIN")
		}
*/
		myQry = myQry.replaceFirst("\\?", "'$questionnaireName'")
		myQry = myQry.replaceFirst("\\?", "'$codProject'")
		// def myQryEscaped = groovy.json.StringEscapeUtils.escapeJava(myQry)
		// def myQryUnescaped = groovy.json.StringEscapeUtils.unescapeJava(myQry)
		// def qName = questionnaireName.getBytes("UTF-8")
		// println("bytes: questionnaire: $questionnaireName $qName vs\n $QES_SPAIN ${QES_SPAIN.getBytes("UTF-8")}")

		def rows = theSqlConn.rows(myQry)

		rows
	}

	/**
	 * Return a list with idinterview, idperformance, idpat
	 * @param codPatient the patient code
	 * @param questionnaireName the questionnaire name
	 * @return a list with rows containing the result
	 */
	def getPatientsPerformances (codPatient, questionnaireName) {
		if (theSqlConn == null)
			this.getDbConn()

		def params = [questionnaireName, codPatient]
		def myQry = ""
/*
		if (questionnaireName.equals(QES_SPAIN)) {
			myQry = this.selPatientPerf.replaceFirst("\\?", "'$questionnaireName'")
			println("performances-> ## equals: $questionnaireName and $QES_SPAIN")
		}
		else {
			myQry = this.selPatientPerf.replaceFirst("\\?", "'$QES_SPAIN'")
			println("performances-> ## differents: $questionnaireName and $QES_SPAIN")
		}
*/
		myQry = this.selPatientPerf.replaceFirst("\\?", "'$questionnaireName'")
		myQry = myQry.replaceFirst("\\?", "'$codPatient'")
		println "$questionnaireName, $codPatient for\n${myQry}"

		// def rows = theSqlConn.rows(this.selPatientPerf, params)
		def rows = theSqlConn.rows(myQry)

		if (rows.size() > 0)
			println "Perf info: idinterview: ${rows[0]['idinterview']}; idperf: ${rows[0]['idperformance']}; idpat: ${rows[0]['idpat']}\n"
		else
			println "No interviews (performances) left for $codPatient and $questionnaireName\n"

		rows
	}




	/**
	 * Get the number of questionnaires which match the name
	 * @param name
	 */
	def getQuestionnaires (name) {
		if (theSqlConn == null)
			this.getDbConn()

		println "getQuestionnaires for $name"
		def rows = []


		/* def qryName = name.tr("ñ", "\u00F1")
		def qryName = name.tr("\u00F1", "ñ")
		println "utf8 -> name: $name vs qryName: $qryName & ${qryName == name}"
		rows = theSqlConn.rows(this.selQuestionnaires, [qryName], {})
		if (rows.size() > 0)
			println ("tokenize worked out!!!!!")
    */

		if (rows.size() == 0) {
			println "... last chance"
			rows = theSqlConn.rows(this.selQuestionnaires.replaceFirst('\\?', "'$QES_SPAIN'"))
		}

		rows
	}

	/**
	 * Returns the name of the questionnaire from the database id
	 * @param dbId, the database id for the questionnaire
	 * @return the name of the questionnaire or null if no questionnaire for the parameter dbId
	 */
	def getQuestionnaireNameFromId (dbId) {
		def record = theSqlConn.firstRow(this.selQuestionnaireFromId.replaceFirst('\\?', dbId))

		record.name
	}



	/**
	 * Update a patient code by changing it from the old one to a new one
	 * @param oldSubjecCode, subject code to be changed
	 * @param newSubjectCode, new code to change
	 * @return the rows affected, 1 if successful, 0 otherwise
	 */
	def updateSubjectCode (oldSubjecCode, newSubjectCode) {
		if (theSqlConn == null)
			this.getDbConn()

		def newCodPrj = newSubjectCode[0..2]
		def newCodHosp = newSubjectCode[3..4]
		def newCodType = newSubjectCode[5]
		def newCodPat = newSubjectCode[6..8]

		def myQry = this.updPatCode.replaceFirst("\\?", newSubjectCode)
		myQry = myQry.replaceFirst("\\?", newCodPrj)
		myQry = myQry.replaceFirst("\\?", newCodHosp)
		myQry = myQry.replaceFirst("\\?", newCodType)
		myQry = myQry.replaceFirst("\\?", newCodPat)
		myQry = myQry.replaceFirst("\\?", oldSubjecCode)

		def rowsAffected = 0
		try {
			rowsAffected = this.theSqlConn.executeUpdate(myQry)
		}
		catch (sqlEx) {
			rowsAffected = -1 // error happend
		}

		rowsAffected
	}

  /**
   * Gets the number of interviews for a patient
   * @param subjectCode the subject code
   * @return the number of interviews (performances) found for a subject
   */
  def getNumIntrvs4Pat (subjectCode) {
    if (theSqlConn == null)
      this.getDbConn()


    def myQry = this.selNumIntrvs4Pat.replaceFirst("\\?", "'$subjectCode'")

    // def rows = theSqlConn.rows(this.selPatientPerf, params)
    def rows = theSqlConn.rows(myQry)

    if (rows.size() > 0)
      println "Num of interviews for $subjectCode: ${rows[0]['counter']}\n"
    else
      println "No performances are left for $subjectCode\n"

    rows[0]['counter']
  }


	/**
	 * Runs arbitrary sql queries untouched
	 * @param sqlQuery, the query which will be run
	 * @return the rows returned by the query
	 */
	def runSQL (sqlQuery) {
		def rows = theSqlConn.rows(sqlQuery)

		rows
	}


// DELETION QUERIES /////////////////////////////////////////////////////
	/* this.dbQuery.deletePerformance(aPerf)
	this.dbQuery.deletePGAQs(idPgas)
	this.dbQuery.deleteAnswers(answers)

	def delOnlyAnswersQry = "delete from answer where idanswer in ("
  answersIdList.each { it ->
    delOnlyAnswersQry += it + ","
  }
  delOnlyAnswersQry = delOnlyAnswersQry.substring(0, delOnlyAnswersQry.length()-1)+")"

  def res = [:]
  def patientsQry = this.patsIdQry.replaceFirst('\\?', codesString)

	*/

	def deletePerformance (aPerf) {
		if (theSqlConn == null)
			this.getDbConn()

		def currentDeletePerf = deletePerf.replaceFirst('\\?', aPerf.toString())
		theSqlConn.execute(currentDeletePerf)
		// println "$currentDeletePerf"
		def rowsAffected = theSqlConn.updateCount

		rowsAffected
	}



	def deletePgas (pgaIdList) {
    if (pgaIdList.size() == 0)
      return 0

		if (theSqlConn == null)
			this.getDbConn()

		def pgaIdListString = ""
		pgaIdList.each { pgaIdListString += it.toString() + "," }
		pgaIdListString = pgaIdListString.substring(0, pgaIdListString.length()-1)

		def currentDeletePGQAs = deletePGQAs.replaceFirst('\\?', pgaIdListString)
		// println "$currentDeletePGQAs"
		theSqlConn.execute(currentDeletePGQAs)

		def rowsAffected = this.theSqlConn.updateCount

		rowsAffected
	}



	def deleteIntrvAnswers (idAnswersList) {
    if (idAnswersList.size() == 0)
      return 0

		if (theSqlConn == null)
			this.getDbConn()

		def idAnsListString = ""
		idAnswersList.each { idAnsListString += it.toString() + "," }
		idAnsListString = idAnsListString.substring(0, idAnsListString.length()-1)

		def currentDeleteAnswers = deleteAnswers.replaceFirst('\\?', idAnsListString)
		// println "$currentDeleteAnswers"
		theSqlConn.execute(currentDeleteAnswers)

		def rowsAffected = this.theSqlConn.updateCount

		rowsAffected
	}

}
