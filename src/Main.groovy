/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Aug 19, 2010
 * Time: 12:36:39 PM
 * To change this template use File | Settings | File Templates.
 */


import groovy.sql.*;
import java.sql.ResultSet
import org.inb.util.QryRow



// STARTS GROOVY SCRIPT /////////////////////////////////////////////////////
String dbUrl = "jdbc:postgresql://localhost:5432/appform"
dbUrl = "jdbc:postgresql://gredos.cnio.es:5432/appform"
String dbUser = "gcomesana"
String dbPass = "appform";

Sql theSql = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

if (theSql != null) {

  String qry = '''select pga.codquestion, pga.codpat, pga.answer_number, pga.answer_order, pga.codanswer,
          ans.thevalue, ans.datecreated, ans.lastupdated
        from pat_gives_answer2ques pga, answer ans,
        (
        select pga.codquestion as idq, pga.codpat as codpat, pga.answer_order as ansord,
            pga.answer_number as ansnum, count (pga.codanswer) as anscount
        from pat_gives_answer2ques pga, answer a
        where pga.codanswer = a.idanswer
        group by 1, 2, 3, 4
        having count (pga.codanswer) > 1
        order by 2, 4, 3
        ) subq
        where pga.codquestion = subq.idq
          and pga.codpat = subq.codpat
          and pga.answer_number = subq.ansnum
          and pga.answer_order = subq.ansord
          and pga.codanswer = ans.idanswer
        order by 1, 2, 3, 4;'''
  def ansDel = "delete from answer where idanswer=";
  def pgaDel = "delete from pat_gives_answer2ques where codanswer="

  // List multipleAns = new ArrayList ();
  multipleAns = []

  def deletedAnsCount = 0
  def processedAnsCount = 0

  def txClosure = { item ->
    println ansDel+item.getIdAnswer()
//    theSql.execute(ansDel+item.getIdAnswer())

    println pgaDel+item.getIdAnswer()
//    theSql.execute(pgaDel+item.getIdAnswer())
    deletedAnsCount++
  }


println ("Processing repeating answers...")
  theSql.query(qry) { ResultSet rs ->
    while (rs.next()) {
      processedAnsCount++

      QryRow row = new QryRow (rs.getInt(QryRow.IDQUESTION_IDX),
                      rs.getInt(QryRow.IDPAT_IDX), rs.getInt(QryRow.ANSNUM_IDX),
                      rs.getInt(QryRow.ANSORD_IDX), rs.getInt(QryRow.IDANS_IDX),
                      rs.getString(QryRow.VALUE_IDX));

// add a row with the same value for the same answer position for the same
// patient for the same question
// if different criteria wants to be applied, the equals method has to be changed
      if (multipleAns.contains(row)) {
        multipleAns += row
        print ("#")
      }
      else { // the new row does not belong to the multiple answers
// first, check if there are more then one row in the multipleAns and delete
// all of them except that with the higher idanswer
        println ()
        if (multipleAns.size() > 1) {

// sort the list by idanser and remove (delete) all items but the last one
// (by convention)
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
/*
            listOthers.each {
              it.remove(it.size()-1);
              it.each {
                item -> theSql.withTransaction { txClosure (item) }
              }
            }
*/
          } // EO if "2 different questions, one is 9999"

          else if (answers.size() == 1) {
// del all items except the last one, as all of them are the same
            def toDel = answers.values() // List<ArrayList<QryRow>>
            toDel.each {
              it.each {
                item -> if (item.compareIdAns (it.last()) == -1) {
//                    println ansDel + item.getIdAnswer ()
                  theSql.withTransaction { txClosure(item) }
                }
                else if (item.compareIdAns(it.last()) == 0)
                    println item.getIdAnswer () + "->" + item.getAnswerVal() + " STAYS"
              }

            } // toDel.each
          } // EO else if answers is 1

          else {
            multipleAns.each {
//              item -> theSql.withTransaction { txClosure (item) }

              item -> if (item.compareIdAns (multipleAns.last()) == -1)
                         theSql.withTransaction { txClosure (item) }
              
                      else if (item.compareIdAns(multipleAns.last()) == 0)
                         println item.getIdAnswer () + "->" + item.getAnswerVal() + " STAYS"
            }
          }
          println "==========="
        } // if multipleAns.size > 1
// clear the list to add a new group of multiple answers
        multipleAns.clear()
        multipleAns += row
        println ("")
      }

    } // EO while
  } // EO closure
  println "Total answers: Processed:${processedAnsCount}; Removed: "+deletedAnsCount

} // EO if