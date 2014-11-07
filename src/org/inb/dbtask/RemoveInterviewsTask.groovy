package org.inb.dbtask

import groovy.sql.Sql
import org.inb.util.DBQuery

/**
 * This task removes interview instances for the patients. It is
 * parametrized by patient code and interview(s) code(s) for those patients.
 */
class RemoveInterviewsTask extends AbstractDBTask {
	private DBQuery dbQuery

	/**
	 * Associative array where the keys are the patient code and, the values,
	 * an array with the INTERVIEW IDs as interview names are not unique!!
	 */
	def patientsIntrvs = [:]
//  def patientsRows = [:]

	// this is actually a map with the following structure:
	// codPatient -> [codeSample -> numquestions]
  def patientsWithSamples
//	def patientsDeleted
	def interviewsDeleted

  def simulation = true // to use select (simulation = true) or delete


  /**
   * Constructor with named parameters patients as a HashMap codpatient->list_of_interviews_to_delet
   * and sim, which is the simulation
   * @params Map args a map to set the optional patients list parameters
   */
  public RemoveInterviewsTask (Map args) {
    this.patientsIntrvs = args.get('interviews', [:])
    this.simulation = args.get('sim', true)

    this.patientsWithSamples = [:] // patients with samples for QES interview
//	  this.patientsDeleted = 0
//	  this.interviewsDeleted = 0
	  this.interviewsDeleted = []

	  this.dbQuery = new DBQuery()
/*
	  println "RemoveInterviews (patientIntrvs):"
	  patientsIntrvs.each {
			println "$it.key: [$it.value]"
	  }
*/
  }



	/**
	 * Performs a controlled deletion of questionnaires (performances and answers).
	 * Controlled is 'cause if form is QES and the patient has samples, the interview
	 * wont be deleted. So, the flow to carry out controller deletion is:
	 * - get the samples for the subject. if any sample returned, abort
	 * - get the performances for subject and questionnaire
	 * - get the answers and pgas for the user-performance
	 * - when got this one, delete performance, entries in pat_gives_answers2ques table and answers
	 * that's it
	 *
	 * @param Sql sql the sql object to run the queries
	 * @param Closure constraint an optional closure which should act as a constraint
	 *
	 * @return the number or total rows deleted
	 */
	public Integer performTask(Sql sql, Closure constraint) {
		def totalRowsAffected = 0

		if (this.simulation)
			println "Performing simulation process..."

		// iterate over the patients map
		// Get the performance(pat, intrv), performance_history(performance[, user]),
		// questions(interview) for the interview, pga (questions, codpat) and
		// answers for the patient-intrv
		// Delete performance (=> perf_history), pgas and answers
		println("** patientsInterviews map elements **")
		this.patientsIntrvs.each { k, v -> println("$k -> $v") }

		this.patientsIntrvs.each { // it.key -> patient; it.value -> interviewList
			def codPat = it.key
			def codPrj = codPat[0..2]
			def intrvList = it.value

			println ""
			intrvList.each { intrv ->
				this.dbQuery.setDbConn(sql)
				// constraint: if QES is to delete but patient has samples, report

				def rsSamples = [:]
				def isConstraintSatisfied = { -> // constraint: interviewName == QES and no samples for patient
					// def intrvName = this.dbQuery.getQuestionnaireNameFromId(intrv)
          /*
					if (intrv.indexOf('QES') != -1) {
						println "Checking for QES samples for $codPat"
						rsSamples = dbQuery.getSamples4Patient(codPat)
						return rsSamples.size() == 0
					}
					else
					*/
						return true;
				}
				/*
				def forms = dbQuery.getQuestionnaires(intrv.replaceAll("ñ", "ñ"))
				println "Got interviews for ($intrv):"
				forms.each {
					println "** interview ${it['name']} (${it['idinterview']})"
				}
				*/
				if (isConstraintSatisfied()) {
					// results: ArrayList<GroovyRowResult>, each element a row
					def performances = this.dbQuery.getPatientsPerformances(codPat, intrv)
					def answers = this.dbQuery.getAnswers4UserIntrv(codPat, codPrj, intrv)

					println("Found: ${performances.size()} perfs and ${answers.size()} answers")

					def aPerf = '', answerIds, idPgas
					if (performances.size() > 0) {
						aPerf = performances.get(0).get('idperformance') // Integer
						answerIds = answers.collect { it.get('idanswer') } //  Integer
						idPgas = answers.collect { it.get('idp_a_q') }

						println("answers info: $answerIds; pgas: $idPgas")

						if (this.simulation) {
							totalRowsAffected++ // for performance
							totalRowsAffected += answerIds.size()
							totalRowsAffected += idPgas.size()
						}
						else
							sql.withTransaction {
								totalRowsAffected += this.dbQuery.deletePerformance(aPerf)
								totalRowsAffected += this.dbQuery.deletePgas(idPgas)
								totalRowsAffected += this.dbQuery.deleteIntrvAnswers(answerIds)
								// println "** ================= **"
							}
						this.interviewsDeleted << [codPat,intrv]
					}

					println "$codPat ($intrv) -> performances: ${performances.size()} is $aPerf && answers: ${answers.size()}"
					println "** ================= **"
				}
				else {
					this.patientsWithSamples[codPat] = rsSamples.values()
					println "$codPat has samples for $intrv"
				}
			} // EO each intrvList

		} // EO patientsIntrvs.each...
		totalRowsAffected
	}


}
