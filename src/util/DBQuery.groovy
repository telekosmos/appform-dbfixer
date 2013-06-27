package util

import groovy.sql.Sql

/**
 * Created with IntelliJ IDEA.
 * User: bioinfo
 * Date: 18/06/13
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public class DBQuery {

  private String dbUrl
  private String dbUsr
  private String dbPasswd
  private Sql theSqlConn

  def selPatSamples = """
    select p.idpat, p.codpatient
    from patient p
    where p.codpatient like '?__'
    order by 2;
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
}
