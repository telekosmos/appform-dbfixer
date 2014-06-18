package test

import org.inb.dbtask.ChangeSubjectsCodeTask
import groovy.sql.*

/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Oct 17, 2012
 * Time: 2:13:25 PM
 * To change this template use File | Settings | File Templates.
 */
class ChangeSubjectsCodeTaskTest extends GroovyTestCase {

  private csctSim, csctLive
  private sqlObj
  private mapPats

  void setUp () {

    mapPats = ['TER011436':'TER511436',
      'TER011440':'TER511440',
      'TER011476':'TER511476',
      'TER011518':'TER511518',
      'TER011638':'TER511638',
      'TER011702':'TER511702',
      'TER011703':'TER511703',
      'TER011704':'TER511704',
      'TER011705':'TER511705',
      'TER011706':'TER511706',
      'TER011707':'TER511707',
      'TER021556':'TER521556',
      'TER021541':'TER521541',
      'TER021530':'TER521530',
      'TER021029':'TER521029',
      'TER021031':'TER521031',
      'TER021135':'TER521135',
      'TER021141':'TER521141',
      'TER021147':'TER521147',
      'TER011714':'TER511714',
      'TER011717':'TER511717',
      'TER011724':'TER511724',
      'TER011727':'TER511727',
      'TER011728':'TER511728',
      'TER011729':'TER511729',
      'TER021001':'TER521001',
      'TER021002':'TER521002',
      'TER021004':'TER521004',
      'TER021005':'TER521005',
      'TER021006':'TER521006',
      'TER021007':'TER521007',
      'TER021008':'TER521008',
      'TER021009':'TER521009',
      'TER021012':'TER521012',
      'TER021013':'TER521013',
      'TER021014':'TER521014',
      'TER021015':'TER521015',
      'TER021016':'TER521016',
      'TER021018':'TER521018'
    ]

    csctSim = new ChangeSubjectsCodeTask(patCodes: mapPats)
    csctLive = new ChangeSubjectsCodeTask(patCodes: mapPats, sim: false)
    String dbUrl = "jdbc:postgresql://localhost:5432/appform"
    String dbUser = "gcomesana"
    String dbPass = "appform"

    sqlObj = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

  }


  void testSqlObjectIsNotNull () {
    assert sqlObj != null
    assert csctSim != null
    assert csctSim.subjectsMap != null
    assert csctSim.simulation == true

    assertNotNull(csctLive)
    assertNotNull(csctLive.subjectsMap)
    assertEquals(csctLive.simulation, false)

  }

  void testSimSubjectsUnchanged () {

    def totalAffected = csctSim.performTask(sqlObj, {})
    assert totalAffected == 0
    assertEquals(csctSim.patientsUnchanged.size(), mapPats.size())
  }

  void testLiveSubjectsUnchanged () {
    def totalAffected = csctLive.performTask(sqlObj, {})
    assert totalAffected == 0
    assertEquals(csctLive.patientsUnchanged.size(), mapPats.size())
  }
}
