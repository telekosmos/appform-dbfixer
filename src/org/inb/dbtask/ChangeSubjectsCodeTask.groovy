package org.inb.dbtask

import groovy.sql.Sql
import org.inb.util.DBQuery

/**
 * The duty of this task is chage subjects code
 */
class ChangeSubjectsCodeTask extends AbstractDBTask {
	private DBQuery dbQuery

	def subjectsMap = [:]
//  def patientsRows = [:]

	// this is actually a map with the following structure:
	// codPatient -> [codeSample -> numquestions]
  def patientsWithSamples
	def patientsUnchanged
  def patientsNonexistent

  def simulation = true // to use select (simulation = true) or delete


  /**
   * Constructor with named parameters patients as a HashMap
   * codpatient->list_of_interviews_to_delete and sim, which is the simulation
   * I.E: new ChangSubjectsCodeTask(patCodes: list, sim: true|false)
   *
   * @params Map args a map to set the optional patients list parameters
   */
  public ChangeSubjectsCodeTask (Map args) {
    this.subjectsMap = args.get('patCodes', [:])
    this.simulation = args.get('sim', true)

    this.patientsWithSamples = [:] // patients with samples for QES interview
	  this.patientsUnchanged = [:]
    this.patientsNonexistent = [:]

	  this.dbQuery = new DBQuery()
/*
	  println "RemoveInterviews (patientIntrvs):"
	  patientsIntrvs.each {
			println "$it.key: [$it.value]"
	  }
*/
  }



	/**
	 *
	 * @param Sql sql the sql object to run the queries
	 * @param Closure constraint an optional closure which should act as a constraint
	 *
	 * @return the number or total rows deleted
	 */
	public Integer performTask(Sql sql, Closure constraint) {
		def totalRowsAffected = 0
		def rowsAffected = -1

		if (this.simulation)
			println "Performing simulation process..."

		// iterate over the patients map
		// Get the performance(pat, intrv), performance_history(performance[, user]),
		// questions(interview) for the interview, pga (questions, codpat) and
		// answers for the patient-intrv
		// Delete performance (=> perf_history), pgas and answers
		println("\n** patCodes map elements **")
		this.subjectsMap.each { k, v -> println("$k -> $v") }

		this.subjectsMap.each { // it.key -> patient; it.value -> interviewList
			def oldPatCode = it.key
			def newPatCode = it.value
			def codPrj = oldPatCode[0..2]
			this.dbQuery.setDbConn(sql)

			def rsSamples = [:]
			// constraint closure
			def isConstraintSatisfied = { -> // constraint: interviewName == QES and no samples for patient
				println "Checking for samples for $oldPatCode"
				rsSamples = this.dbQuery.getSamples4Patient(oldPatCode)

				return rsSamples.size() == 0
			}

			if (isConstraintSatisfied()) {  // no samples
			// results: ArrayList<GroovyRowResult>, each element a row
				sql.withTransaction {
          def oldPatRow = this.dbQuery.runSQL("select idpat, codpatient from patient where codpatient=$oldPatCode")
					if (!this.simulation)
            // if 'source' patient does not exists, don't update, just return -1
						rowsAffected = oldPatRow.size() == 0? -1: this.dbQuery.updateSubjectCode(oldPatCode, newPatCode);

					else {
						def newPatRow = this.dbQuery.runSQL("select idpat, codpatient from patient where codpatient=$newPatCode")

						// One subject code is changed only if, in addition to satisfy constraints,
						// the old code exists in DB and the new one not.
						// Rest of combinations are discarded
						if (oldPatRow.size() == 1 && newPatRow.size() == 0)
							rowsAffected = 1

						else if (oldPatRow.size() == 1 && newPatRow.size() == 1)
              rowsAffected = 0

            else if ((oldPatRow.size() == 0 && newPatRow.size() == 0) ||
										 (oldPatRow.size() == 0 && newPatRow.size() == 1))
							rowsAffected = -1
					}
				}

				if (rowsAffected == -1)
					this.patientsNonexistent[oldPatCode] = newPatCode

        else if (rowsAffected == 0)
          this.patientsUnchanged[oldPatCode] = newPatCode

				else // rowsAffectd is 1
					totalRowsAffected++

			}
			else {
				this.patientsWithSamples[oldPatCode] = rsSamples.values()
				println "$oldPatCode has samples: ${this.patientsWithSamples[oldPatCode]}"
			} // EO if

		} // EO subjectsMap.each...
		totalRowsAffected
	}


}
