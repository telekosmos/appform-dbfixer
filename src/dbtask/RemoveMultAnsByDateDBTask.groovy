package dbtask
/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Aug 31, 2010
 * Time: 6:33:49 PM
 * To change this template use File | Settings | File Templates.
 */

import java.sql.ResultSet
import groovy.sql.Sql
import util.QryRow

public class RemoveMultAnsByDateDBTask extends AbstractDBTask {

  private int deletedAnsCount = 0;
  private int ansCount = 0;
  private final String ANSDEL_SQL = "delete from answer where idanswer="

  private boolean simulate = true;

  public RemoveMultAnsByDateDBTask(boolean simulation) {
    super()

    simulate = simulation
  }

  /**
   *
   */
  private void deletionRule(List multipleAns, Sql theSql) {
// first, check if there are more then one row in the multipleAns
// then, sort by creation date
    if (multipleAns.size() > 1) {
// first, sort by idanswers. this is done in the case of all
// dates are the same or null.
      multipleAns.sort {
        row1, row2 -> row1.getIdAnswer() <=> row2.getIdAnswer()
      }

// first sort, reverse order by creation date: first one is supossed to be
// the last one in being added
      multipleAns.sort {
        row1, row2 -> row2.getDateCreated() <=> row1.getDateCreated()
      } // to define

      println "1st sort"
      multipleAns.each {
        println it.getIdAnswer() + " = " + it.getAnswerVal() + " (" + it.getDateCreated() + ")"
      }
      println "***************"

// reverse sort by updatedDate value in order to sort by datecreated and,
// if similar or some of the answer updated, set the right order to remove
// all elements except the last one
      multipleAns.sort {
        row1, row2 -> row2.getLastUpdate() <=> row1.getLastUpdate()
      }

      println "2nd sort"
      multipleAns.each {
        println it.getIdAnswer() + " = " + it.getAnswerVal() + " (" + it.getLastUpdate() + ")"
      }
      println "==============="

// remove all questions except the one
// they're reversely sorted by date
      // all items with a null value as answer
      def ansValNulls = multipleAns.findAll {
        it.getAnswerVal() == null
      }

      def diffList = multipleAns - ansValNulls


      QryRow lastItem = multipleAns.last()
      lastItem = diffList.size() == 0 ? lastItem : diffList.last()
      QryRow firstItem = multipleAns.first()
      firstItem = diffList.size() == 0 ? firstItem : diffList.first()

      QryRow no9999item = multipleAns.find {
        it.getAnswerVal() != null &&
          it.getAnswerVal().compareTo("9999") != 0
      }

// This is the reference item which means the item which will be preserved from deletion
      QryRow itemRef = no9999item != null ? no9999item : firstItem
      println("itemRef -> ${itemRef.getIdAnswer()}: ${itemRef.getAnswerVal()}")
      multipleAns.each {
        item -> // if (item.compareIdAns (multipleAns.first()) != 0)
        if (item.compareIdAns(itemRef) != 0)
          theSql.withTransaction {
            println "#${ansCount}#.- " + ANSDEL_SQL + item.getIdAnswer()
            if (this.simulate == false)
              theSql.execute(ANSDEL_SQL + item.getIdAnswer());

            deletedAnsCount++;
          }
      }
    }// EO while

  } // EO deletionRule


  public Integer performTask(Sql theSql) {

    def multipleAns = []
    ansCount = 0;
	  println "QryRow.performTask:\n ${this.qry}"
    theSql.query(this.qry) { ResultSet rs ->
      while (rs.next()) {
        QryRow row = new QryRow(rs.getInt(QryRow.IDQUESTION_IDX),
          rs.getInt(QryRow.IDPAT_IDX), rs.getInt(QryRow.ANSNUM_IDX),
          rs.getInt(QryRow.ANSORD_IDX), rs.getInt(QryRow.IDANS_IDX),
          rs.getString(QryRow.VALUE_IDX),
          rs.getTimestamp(QryRow.CREATION_IDX),
          rs.getTimestamp(QryRow.UPDATE_IDX));

// add a row with the same value for the same answer position for the same
// patient for the same question
// if different criteria wants to be applied, the equals method has to be changed
        if (multipleAns.contains(row)) {
          multipleAns += row
          ansCount++
          println "(q" + row.getIdQuestion() + "-" + row.getAnsNumber() + "-" + row.getAnsOrder() +
            ") " + row.getIdAnswer() + "-" + row.getAnsNumber() + "-" + row.getAnsOrder() +
            " -> created: " + row.getDateCreated() + "; updated: " + row.getLastUpdate()
        }

        else { // the new row does not belong to the multiple answers
          deletionRule(multipleAns, theSql);
          println "============xxxxxxxxxxxxxxxx=============="
          multipleAns.clear()

          multipleAns += row
          ansCount++
          println "(q" + row.getIdQuestion() + "-" + row.getAnsNumber() + "-" + row.getAnsOrder() +
            ") " + row.getIdAnswer() + "-" + row.getAnsNumber() + "-" + row.getAnsOrder() +
            " -> created: " + row.getDateCreated() + "; updated: " + row.getLastUpdate()
        }

      } // while

      deletionRule(multipleAns, theSql)
      println "============xxxxxxxxxxxxxxxx=============="
    } // CLOSURE

    println "Total answers retrieved: ${ansCount}; deleted: ${deletedAnsCount}"

	  ansCount
  } // performTask


  public String test () {
    "this is only a test when simulation is ${this.simulate}"
  }

}