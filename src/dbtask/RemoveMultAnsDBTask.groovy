package dbtask
/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Aug 31, 2010
 * Time: 5:56:29 PM
 * To change this template use File | Settings | File Templates.
 */

import groovy.sql.*
import java.sql.ResultSet

public class RemoveMultAnsDBTask extends AbstractDBTask {



  public RemoveMultAnsDBTask () {
    super ()
  }



/**
 * Performa a task over the rs content
 */
  public Integer performTask (Sql theSql) {

    def multipleAns = []
    def txClosure = { item ->
      println ansDel+item.getIdAnswer()
//      theSql.execute(ansDel+item.getIdAnswer())


//      println pgaDel+item.getIdAnswer()
//      theSql.execute(pgaDel+item.getIdAnswer())
      deletedAnsCount++
    }


    theSql.query (this.qry) { ResultSet rs ->
      while (rs.next()) {

        QryRow row = new QryRow (rs.getInt(QryRow.IDQUESTION_IDX),
                        rs.getInt(QryRow.IDPAT_IDX), rs.getInt(QryRow.ANSNUM_IDX),
                        rs.getInt(QryRow.ANSORD_IDX), rs.getInt(QryRow.IDANS_IDX),
                        rs.getString(QryRow.VALUE_IDX));

  // add a row with the same value for the same answer position for the same
  // patient for the same question
  // if different criteria wants to be applied, the equals method has to be changed
        if (multipleAns.contains(row))
          multipleAns += row

        else { // the new row does not belong to the multiple answers
  // first, check if there are more then one row in the multipleAns and delete
  // all of them except that with the higher idanswer
          if (multipleAns.size() > 1) {

  // sort the list by idanswer
            multipleAns.sort { aRow -> aRow.getIdAnswer() }
  //          def answers = [:]
  //          multipleAns.each { item -> answers[item.getIdAnswer()]=item.getAnserVal() }

  // the next one returns a Map<answerval, ArrayList<QryRow>
            def answers = multipleAns.groupBy { it.getAnswerVal () }
            println ("num of different answers: "+answers.size())
            answers.each { println it.key+" -> "+it.value }

  // remove 9999 if possible
            if (answers.keySet().contains("9999") && answers.size() == 2) {
              def nines = answers.findAll { it.key.equalsIgnoreCase("9999")}
              def others = answers.findAll { it.key.equalsIgnoreCase("9999") == false }
              assert nines instanceof Map<String, List>;
              def listNines = nines.values ()
              def listOthers = others.values()

              listNines.each {
                it.each {
                  item -> theSql.withTransaction { txClosure (item) }
                }
              }
              listOthers.each {
                it.remove(it.size()-1);
                it.each {
                  item -> theSql.withTransaction { txClosure (item) }
                }
              }

            } // EO if "2 different questions, one is 9999"
            else if (answers.size() == 1) {
  // del all items except the last one, as all of them are the same
              def toDel = answers.values() // List<ArrayList<QryRow>>
              toDel.each {
                it.each {
                  item -> if (item.compareIdAns (it.last()) == -1) {
                    /*
                              println ansDel + item.getIdAnswer ()
  //                            theSql.execute(sqlDel+item.getIdAnswer())
                              deletedAnsCount++
                     */
                            theSql.withTransaction { txClosure(item) }
                          }
                          else if (item.compareIdAns(it.last()) == 0)
                              println item.getIdAnswer () + "->" + item.getAnswerVal() + " STAYS"
                }

              } // toDel.each
            } // EO else if answers is 1

  /*
            multipleAns.each { item -> if (item.compareIdAns (multipleAns.last()) == -1)
                                          println sqlDel + item.getIdAnswer ()
                                       else if (item.compareIdAns(multipleAns.last()) == 0)
                                          println item.getIdAnswer () + "->" + item.getAnswerVal() + " STAYS"
                             }
  */
            println "==========="
          } // if multipleAns.size > 1
  // clear the list to add a new group of multiple answers
          multipleAns.clear()
          multipleAns += row
        }

      } // EO while
    } // closure

	  return 1
  } // EO performTask

}