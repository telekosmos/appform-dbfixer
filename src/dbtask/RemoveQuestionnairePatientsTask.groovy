package dbtask

import groovy.sql.Sql
import util.DBQuery

/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Oct 17, 2012
 * Time: 11:55:26 AM
 * To change this template use File | Settings | File Templates.
 */
class RemoveQuestionnairePatientsTask extends AbstractDBTask {

  def delAnswersQry = """delete from answer where idanswer in (
      select a.idanswer -- , a.thevalue
      from patient p, pat_gives_answer2ques pga, answer a, question q, item it
      where p.codpatient = ?
        -- and p.idpat = ?
        and p.idpat = pga.codpat
        and pga.codanswer = a.idanswer
        and pga.codquestion = q.idquestion
        and q.idquestion = it.iditem
        and q.idquestion in
            (select idquestion
             from question q, item i, section s, interview iv, project p
             where 1 = 1 -- s.codinterview = it.idinterview
               and p.project_code = '157'
               and iv.codprj = p.idprj
               and upper(iv.name) = upper('Formulario de participación')
               and s.codinterview = iv.idinterview
               and i.idsection = s.idsection
               and q.idquestion = i.iditem );
  """

// params for selAnswersQry are codpatient, codproject and questionnaire name
  def selAnswersQryQuestionnaire = """select a.idanswer, a.thevalue, p.idpat, p.codpatient
      from patient p, pat_gives_answer2ques pga, answer a, question q, item it
      where p.codpatient = ?
        -- and p.idpat = ?
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
      -- and p.idpat = ?
      and p.idpat = pga.codpat
      and pga.codanswer = a.idanswer
      and pga.codquestion = q.idquestion
      and q.idquestion = it.iditem
      and q.idquestion in
          (select idquestion
           from question q, item i, section s, interview iv, project p
           where 1 = 1 -- s.codinterview = it.idinterview
             -- and upper(iv.name) = upper(?)
             and p.project_code = ?
             and iv.codprj = p.idprj
             and s.codinterview = iv.idinterview
             and i.idsection = s.idsection
             and q.idquestion = i.iditem );
  """


  def patsIdQry = """select p.idpat as idpat, p.codpatient as codpatient
    from patient p
    where p.codpatient in (?)
  """


  def delPGAQRows = """delete
    from pat_gives_answer2ques pga
    where codpat = ?;
  """


  def selPGAQRows = """select * -- delete
    from pat_gives_answer2ques pga
    where codpat = ?;
  """


  def delPatient = """
    delete from patient
    where codpatient = ?
    and idpat = ?;
  """


  def selPatient = """
    select *
    from patient
    where codpatient = ?
    and idpat = ?;
  """


  def selPrjCode = """
    select idprj
    from project
    where lower(name) = lower(?);
  """


  def selPatSamples = """
    select p.idpat, p.codpatient
    from patient p
    where p.codpatient like '?__'
    order by 2;
  """


  def patientsCode = []
  def patientsRows = [:]

  def patientsWithSamples
	def patientsDeleted

  def simulation = true // to use select (simulation = true) or delete


  /**
   * Constructor with named parameters patients list
   * @params Map args a map to set the optional patients list parameters
   */
  public RemoveQuestionnairePatientsTask(Map args) {
    this.patientsCode = args.get('patients', [])
    this.simulation = args.get('sim', true)

    this.patientsWithSamples = []
	  this.patientsDeleted = 0
  }


  /**
   * Performs a task, which will be performed by some sql statement(s) and methods.
   * This is an interface method. For this class, the task to perform is patients and
   * associated data deletion.
   * The patient deletion is performed through the following steps:
   * - patient's answers deletion
   * - deletion of rows in pat_gives_ans2ques table where the patient is involved
   * - patient deletion
   * Each of this step is performed by one sql query (delete statement)
   * @param Sql sql, the sql object which is bound to the database to perform the task
   * @return a Map (Hash) with the codpatients as valuess and the database id for the patient as keys
   *
   public void performTask (Sql sql) {
   if (this.simulation)
   println("Performing simulation process...")

   def stringCodes = []
   this.patientsCode.each () { it ->
   stringCodes << "'"+it.split(',')+"'"
   }def codesString = stringCodes.join(',')

   def res = [:]
   def patientsQry = this.patsIdQry.replaceFirst('\\?', codesString)
   sql.eachRow(patientsQry) { row ->
   this.patientsRows[row.idpat] = row.codpatient

   sql.withTransaction {

   def ansDeleted = deleteAnswers4Patient (sql, row)
   def pgasDeleted = deletePGAQEntries(sql, row)
   def patAffected = deletePatient (sql, row)
   println ("answers: $ansDeleted (pgas: $pgasDeleted); patients affected: $patAffected")
   }

   }
   }
   */
  public Integer performTask(Sql sql, Closure constraint) {

    if (this.simulation)
      println "Performing simulation process..."

// Decompose the line into its components separately: codpatient,questionnaire info,prj info
    this.patientsCode.each { it ->
      def currentPatient = it.tokenize(',')
      def prjCode = currentPatient[0].substring(0, 3)
      /*
      if (!prjCode.isNumber()) {
        def ans = sql.firstRow(selPrjCode, [currentPatient])
        prjCode = ans.idprj
        currentPatient
      }
//      currentPatient[2] = prjCode
//      currentPatient << prjCode
      */
      currentPatient[2] = prjCode
      def answers4Pat = sql.rows(selAnswersQryQuestionnaire, currentPatient)
      if (answers4Pat.size() == 0) return
      
      def answersId = answers4Pat.collect { it[0] }

      sql.withTransaction {
	      def codeSamples = getSamples4Subject(sql, currentPatient)

        if (currentPatient.length() == 9 && codeSamples.size() > 0) {
          this.patientsWithSamples << currentPatient
        }
	      else { // delete if sample or normal subject w/o smaples
	        def ansDeleted = deleteAnswers4Patient(sql, answersId)
	        def pgasDeleted = deletePGAQEntries(sql, answersId)
	        def patAffected = deletePatient(sql, [answers4Pat[0][2],answers4Pat[0][3]])
	        this.patientsDeleted++
	        println("answers: $ansDeleted (pgas: $pgasDeleted); patients affected: $patAffected")
        }
      } // EO sql
    } // EO patientsCode for task

	  this.patientsDeleted
  }



 /**
  * Gets the list of patients with samples. If run before task performing, it
  * always will be an empty list
  * @return a list of strings with the patients code with samples
  */
	def getSubjectsWithSamples () {
		return this.patientsWithSamples
	}




  def getSamples4Subject (sqlObj, codPatient) {
    DBQuery dbq = new DBQuery(sqlObj)
    def sampleCodes = dbq.getSamples4Patient(codPatient)

    sampleCodes
  }


/**
 * Gets a map of elements idpat:codpatient out of a list of patient codes.
 * @param Sql sqlObj, the sql object to interace with the database
 * @param listCodes , an array or list with patient codes as strings
 * @return a map or hash with idpat:codpatient elements
 */
  def getPatientsFromCodes(sqlObj, listCodes) {

    def stringCodes = []
    this.patientsCode.each() { it ->
      stringCodes << "'" + it + "'"
    }
    def codesString = stringCodes.join(',')

    def res = [:]
    def patientsQry = this.patsIdQry.replaceFirst('\\?', codesString)
    def rows = []

    rows = sqlObj.rows(patientsQry)
    rows.each { row ->
      res[row.idpat] = row.codpatient
    }
    rows
  }




/**
 * Deletes the answers for that patient.
 * @param Sql sqlObj, the sql object to interace with the database
 * @param List answersList, a list with the database id of the answers to delete
 * @xparam ArrayList patientInfo, the three-element array with codpatient, questionnaireCode, prjCode
 * @xparam Map patCodesHash, a pair as a map with the form idpat:codpatient
 */
  def deleteAnswers4Patient(sqlObj, answersIdList) {

//    def mierdiList = [patCodesHash.codpatient, patCodesHash.idpat]
    def answers = [], res = 0
//    answers = sqlObj.rows(selAnswersQry, patientInfo)

    def delOnlyAnswersQry = "delete from answer where idanswer in ("
    answersIdList.each { it ->
      delOnlyAnswersQry += it + ","
    }
    delOnlyAnswersQry = delOnlyAnswersQry.substring(0, delOnlyAnswersQry.length()-1)+")"

    if (this.simulation) {
//      println "answers number: ${answersIdList.size()}"
//      sqlObj.execute (delOnlyAnswersQry)
//      println "sim - $delOnlyAnswersQry"
      res = answersIdList.size()
    }
    else {
      sqlObj.execute (delOnlyAnswersQry)
//      println "$delOnlyAnswersQry"
      res = sqlObj.updateCount
    }
    res
  }



  
/**
 * Deletes the entries in the 'pat_gives_answer2question' table to remove the orphan
 * rows connection patients-questions-answers.
 * @param Sql sqlObj, the sql object to interace with the database
 * @param List answersList, a list with the database id of the answers to delete
 * @xparam Map patCodesHash, a pair as a map with the form idpat:codpatient
 */
  def deletePGAQEntries(sqlObj, answersIdList) {

    def res = false
    def delPgaQry = "delete from pat_gives_answer2ques pga where pga.codanswer in ("
    answersIdList.each { delPgaQry += it + "," }
    delPgaQry = delPgaQry.substring(0, delPgaQry.length() - 1) + ")"

    if (this.simulation) {
//      def pgas = sqlObj.rows(selPGAQRows, [patCodesHash.idpat])
//      res = pgas.size()
      println "answers number: ${answersIdList.size()}"
//      sqlObj.execute (delOnlyAnswersQry)
//      println "sim - $delPgaQry"
      res = answersIdList.size()
    }
    else {
      sqlObj.execute(delPgaQry)
      res = sqlObj.updateCount
    }
    res
  }

  


/**
 * Just delete the patient from the database
 * @param Sql sqlObj, the sql object to interace with the database
 * @param Map patInfo, a pair as a list with the first elem patient db id and the
 * second one refering the patient code
 */
  def deletePatient(sqlObj, patInfo) {
    def res = false
    def patients = []

    if (this.simulation) {
      patients = sqlObj.rows(selPatient, [patInfo[1], patInfo[0]])
      res = patients.size()
      def patId = patInfo[1]
      println "delete from patient where codpatient = $patId"
    }
    else {
      res = sqlObj.execute(delPatient, [patInfo[1], patInfo[0]])
      res = sqlObj.updateCount
    }
    res
  }
}
