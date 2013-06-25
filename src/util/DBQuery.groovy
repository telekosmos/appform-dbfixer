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



  public DBQuery () {
    super();
  }


  public DBQuery (dbUrl, dbUser, dbPassword) {
    this.dbPasswd = dbPassword
    this.dbUrl = dbUrl
    this.dbUsr = dbUser
  }


  public DBQuery (dbConn) {
    this.theSqlConn = dbConn
  }


  def Sql getDbConn () {
    theSqlConn = Sql.newInstance(this.dbUrl, this.dbUsr, this.dbPasswd, 'org.postgresql.Driver')
    theSqlConn
  }


  def closeDbConn () {
    theSqlConn.close();
  }


  /**
   * Gets the codes for the samples related to this patient
   * @param String codPatient the patient code
   * @return a list with the codes for the samples, if any found
   */
  def getSamples4Patient (codPatient) {
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


}
