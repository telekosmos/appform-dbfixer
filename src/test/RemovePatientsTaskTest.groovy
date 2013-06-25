package test

import dbtask.RemoveQuestionnairePatientsTask
import groovy.sql.*

/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Oct 17, 2012
 * Time: 2:13:25 PM
 * To change this template use File | Settings | File Templates.
 */
class RemovePatientsTaskTest extends GroovyTestCase {

  private rpt
  private sqlObj
  private listPats

  void setUp () {
    listPats = ['157071001', '157071004', '157071005', '157071026',
        '157071027', '157072025']
    rpt = new RemoveQuestionnairePatientsTask(patients: listPats)
    String dbUrl = "jdbc:postgresql://localhost:4321/appform"
    String dbUser = "gcomesana"
    String dbPass = "appform"

    sqlObj = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

  }


  void testSqlObjectIsNotNull () {
    assert sqlObj != null
    assert rpt != null
    assert rpt.patientsRows.length == 0
  }

  void testNoPatients () {
    rpt.performTask(sqlObj)

    assertEquals (rpt.patientsRows.length, listPats.length)
    rpt.patientsRows.eachWithIndex { it, i ->
      assert listPats.contains(it.value)
    }
  }
}
