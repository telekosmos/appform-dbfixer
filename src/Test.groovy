

import dbtask.RemovePatientsTask
import groovy.sql.Sql

/*
def printClosure = { a -> println a }
println "This is only a test"
def map = ['a':3, 'b':9999, 'c':9999, 'd':3];
def valsMap = map.values()
def uniqueVals = valsMap.groupBy { it }
println "different vals: ${uniqueVals.size()}"
uniqueVals.each { it -> println it }
println "================="
uniqueVals.each { item -> printClosure (item)}
*/


// listPats = ['157071001', '157071004', '157071005', '157071026',
   // '157071027', '157072025']
listPats = ['188011009', '157102002', '157091049', '157091003', '157091023',
						'157102002','157401002', '57F093003',	'57F093007', '162093003',
						'162091001']

rpt = null
sqlObj = null

def setUp () {

  rpt = new RemovePatientsTask(patients: listPats, simulation: true)
  String dbUrl = "jdbc:postgresql://localhost:4321/appform"
  String dbUser = "gcomesana"
  String dbPass = "appform"

  sqlObj = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

}


def setDown () {
  sqlObj.close()
}


def testSqlObjectIsNotNull() {
  assert sqlObj != null
  assert rpt != null
  assert rpt.simulation
  assert rpt.patientsRows.size() == 0
  assert rpt.patientsCode.size() == listPats.size()
}


def testNoPatients() {
  rpt.performTask(sqlObj)

  assert rpt.patientsRows
  assert rpt.patientsRows.size() == listPats.size()
  rpt.patientsRows.eachWithIndex {it, i ->
    assert listPats.contains(it.value)
  }
}



def testNumPatients () {
  patients = rpt.getPatientsFromCodes(sqlObj, listPats)
  assert patients != null
  assert patients.size() == listPats.size()

  patients.each { pat ->
    assert listPats.contains(pat.codpatient)
  }

}



def testAnswers4Pats () {
  patients = rpt.getPatientsFromCodes(sqlObj, listPats)
  patients.each { pat ->
    answers = rpt.deleteAnswers4Patient(sqlObj, pat)
    assert answers != null
    assert answers > 0
  }

}



def testPgaRows () {

  patients = rpt.getPatientsFromCodes(sqlObj, listPats)
  patients.each { pat ->
    answers = rpt.deleteAnswers4Patient(sqlObj, pat)
    pgas = rpt.deletePGAQEntries (sqlObj, pat)

    assert pgas != null
    assert pgas > 0
    assert pgas == answers
  }
}


def testDelPatients () {
	def deletions = rpt.performTask(sqlObj)
	assert deletions >= 0

	def patWithSamples = rpt.getSubjectsWithSamples()
	patWithSamples.each { it ->
		println("$it")
	}
}


setUp()
/*
testSqlObjectIsNotNull()
// testNoPatients()
testNumPatients()
testAnswers4Pats ()
testPgaRows ()
*/
testDelPatients()


setDown()