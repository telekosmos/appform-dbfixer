import dbtask.FixingTasksHub
import dbtask.RemoveInterviewsTask
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
listPats = ['188011009', '157091049', '157091003', '157091023',
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
  assert rpt.patientsIntrvs.size() == listPats.size()
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

	println "Patients with samples:"
	def patWithSamples = rpt.getSubjectsWithSamples() // this is a map subject -> map of samples
	patWithSamples.each {
		println "subject code is $it.key"
		it.value.each { samplesMap ->
			println "$samplesMap.key has $samplesMap.value answers"
		}
	}
	println "** Subjects deleted: $deletions **"
}


def testDelInterviews () {
	// HashMap<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>()
	/*

	m.put('157071001', ['Dieta'])
	m.put('157071004', ['Dieta'])
	m.put('157071005', ['Dieta', 'Calidad de entrevista'])
	m.put('157071026', ['Dieta'])
	m.put('157071027', ['Dieta'])
	m.put('157072023', ['Calidad de entrevista'])
	m.put('157072025', ['Calidad de entrevista'])
  */


	def fuckMap = [
		// '157071001': ['Dieta'],
		// '157071004':['Dieta'],
		'157071005':['QES_Español','Dieta','Calidad_Entrevista'],
		// '157071026':['Dieta'],
		// '157071027':['Dieta'],
		'157071068':['QES_Español'],
		// '157072023':['Calidad_Entrevista'],
		// '157072025':['Calidad_Entrevista'],
		'157072050':['QES_Español'],
		'157072202':['QES_Español']
	]

/*
	def task = new RemoveInterviewsTask(interviews: fuckMap, sim: true)

	// return true if the constraint is satisfied, then the plan can be carried out
	def constraint = { caller ->
		println "In the closure!!"
		if (caller.intrv.indexOf('QES') != -1) {
			println "Checking for QES samples for ${caller.codPat}"
			def rsSamples = caller.dbQuery.getSamples4Patient(caller.codPat)
			return rsSamples.size() == 0
		}
		else
			return true
	}
*/

	FixingTasksHub fs = new FixingTasksHub()
	Map jsonMap = fs.deleteInterviews('localhost', 'gcomesana', 'appform', false, fuckMap);
/*
	def totalRowsDeleted = task.performTask(sqlObj, {})
	def patsWithSamples = task.getPatientsWithSamples()
	def interviewsDel = task.getInterviewsDeleted()
*/
	jsonMap.each {
		println "** ${it.key} => ${it.value}"
	}
	// println "totalRows: $totalRowsDeleted; pats with samples: $patsWithSamples; interviews removed: $interviewsDel\n"

}


setUp()
/*
testSqlObjectIsNotNull()
// testNoPatients()
testNumPatients()
testAnswers4Pats ()
testPgaRows ()
*/
// testDelPatients()

testDelInterviews()
println "\nSe finé\n"
setDown()