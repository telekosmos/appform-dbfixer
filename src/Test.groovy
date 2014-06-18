import org.inb.dbtask.FixingTasksHub
import org.inb.dbtask.RemovePatientsTask
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

def rpt = null
def sqlObj = null
final CHANGING_CODES_FILE = "/Users/telekosmos/DevOps/epiquest/appform-dbfixer/ter-subjects.txt"

def setUp () {
  println "Setting up tests"
  rpt = new RemovePatientsTask(patients: listPats, simulation: true)
  String dbUrl = "jdbc:postgresql://localhost:5432/appform"
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
	// def deletions = rpt.performTask(sqlObj)
	// assert deletions >= 0

	FixingTasksHub fs = new FixingTasksHub()
  println "\ntestDelPatients..."
  def simulation = true
	Map jsonMap =
			fs.deletePatients ('localhost', 'gcomesana', 'appform', simulation, listPats);

	println "Patients with samples:"
	// def patWithSamples = rpt.getSubjectsWithSamples() // this is a map subject -> map of samples
	def patWithSamples = jsonMap.pats_with_samples
	patWithSamples.each {
		println "subject code is $it.key"
		it.value.each { samplesMap ->
			println "$samplesMap.key has $samplesMap.value answers"
		}
	}

	def deletions = jsonMap.rows_affected
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

  println "testDelInterviews"
	def fuckMap = [
		'157071001': ['Dieta'],
		'157071004':['Dieta'],
		'157071005':['QES_Español','Dieta','Calidad_Entrevista'],
		'157071026':['Dieta'],
		'157071027':['Dieta'],
		'157071068':['QES_Español'],
		'157072023':['Calidad_Entrevista'],
		'157072025':['Calidad_Entrevista'],
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
  def sim = true
	Map jsonMap = fs.deleteInterviews('localhost', 'gcomesana', 'appform', sim, fuckMap);
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



def testChangePatsCode () {
  def simulation = false
	def patcodesMap = [
		"157072005":"157072017",
		"157072008":"157072048",
		"157072031":"157072053",
		"157072012":"157072056",
		"157072025":"157072072",
		"157072029":"157072074",
		"157072019":"157072073",
		"157072030":"157072084",
		"157072020":"157072080"
  ]

  patcodesMap = [
    "TER011732": "TER511732",
    "TER021546": "TER521546",
    "TER021536": "TER521536",
    "TER021543": "TER521543",
    "TER021544": "TER521544",
    "TER021566": "TER521566",
    "TER021562": "TER521562",
    "TER021564": "TER521564",
    "TER021565": "TER521565",
    "TER021570": "TER521570",
    "TER021567": "TER521567",
    "TER021527": "TER521527",
    "TER021552": "TER521552",
    "TER021553": "TER521553",
    "TER021557": "TER521557",
    "TER021558": "TER521558",
    "TER021466": "TER521466"
  ]

  def dbUrl = 'jdbc:postgresql://localhost:5432/appform';
	FixingTasksHub fs = new FixingTasksHub(dbUrl, 'gcomesana', 'appform')
	def jsonMap = fs.changeSubjecsCode('localhost', simulation, patcodesMap)

	jsonMap.each {
		println "** ${it.key} => ${it.value}"
	}
}

def testReadfileForChangingCodes(filename) {
  def dbUrl = 'jdbc:postgresql://localhost:5432/appform';
  FixingTasksHub fs = new FixingTasksHub(dbUrl, 'gcomesana', 'appform')
  Map codes = fs.getSubjectCodesMapFromFile(filename)

  assert codes != null
  assert codes.size() > 0
  codes.each {
    def key = it.getKey()
    def val = it.getValue()
    assert ((String)key).indexOf("TER02") != -1
    assert ((String)val).indexOf("TER52") != -1
  }
}



def testParseFisForChangingCodes(filename) {
  def dbUrl = 'jdbc:postgresql://localhost:5432/appform';
  FixingTasksHub fs = new FixingTasksHub(dbUrl, 'gcomesana', 'appform')
  InputStream is = (new File(filename)).newDataInputStream()
  Map codes = fs.parseSubjectCodesFile(is)

  assert codes != null
  assert codes.size() > 0
  codes.each {
    def key = it.getKey()
    def val = it.getValue()
    assert ((String)key).indexOf("TER02") != -1
    assert ((String)val).indexOf("TER52") != -1
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

// testDelPatients()
// testDelInterviews ()
// testChangePatsCode()

println("testReadfileForChangingCodes")
testReadfileForChangingCodes(CHANGING_CODES_FILE)
println("testParseFisForChangingCodes")
testParseFisForChangingCodes(CHANGING_CODES_FILE)

println "\nSe finé\n"
setDown()