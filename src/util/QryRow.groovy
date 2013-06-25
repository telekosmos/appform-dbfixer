package util


import java.sql.Timestamp



/**
 * This class encapsulates a row with the following form:
 * idq  idpat n o idans   ansval
 * -----------------------------
 * 1183	4761	1	1	3437852	9999
 * 1183	4761	1	1	3437957	1
 * 1183	4761	1	1	3437963	1
 * 1183	4761	1	1	3437853	9999
 * 1183	4761	1	1	3437888	1
 */
public class QryRow {

  public static final int IDQUESTION_IDX = 1
  public static final int IDPAT_IDX = 2
  public static final int ANSNUM_IDX = 3
  public static final int ANSORD_IDX = 4
  public static final int IDANS_IDX = 5
  public static final int VALUE_IDX = 6
	public static final int CREATION_IDX = 7
	public static final int UPDATE_IDX = 8

  Integer idQuestion
  Integer idPat
  Integer ansNumber
  Integer ansOrder
  Integer idAnswer
  String ansValue
	Timestamp dateCreated
	Timestamp lastUpdate


  public QryRow (Integer idQue, Integer idPat, Integer ansNum, Integer ansOrd,
                Integer idAns, String ansVal, Timestamp creationDate, Timestamp lastUpd) {
    this.idQuestion = idQue;
    this.idPat = idPat
    this.ansNumber = ansNum
    this.ansOrder = ansOrd
    this.idAnswer = idAns
    this.ansValue = ansVal
	  this.dateCreated = creationDate
	  this.lastUpdate = lastUpd
  }



/**
 * This is a method to compare (for this particular purpose) to rows.
 * @param row, another instance of this class to compare with <code>this</code>
 * object
 * @return true if the two objects are equals; false otherwise
 */
  public boolean equals (Object paramRow) {

    if (paramRow == null)
      return false;

    QryRow row = (QryRow)paramRow;
    boolean areEquals = this.idQuestion == row.idQuestion &&
                this.idPat == row.idPat &&
                this.ansOrder == row.ansOrder &&
                this.ansNumber == row.ansNumber /* &&
                this.ansValue == row.ansValue */

    return areEquals;
  }


  public Integer getIdAnswer () {
    return this.idAnswer;
  }


  public String getAnswerVal () {
    return this.ansValue;
  }


  public String comparisonVals () {
    return this.idQuestion +" | " +this.idPat + " | " +
           this.ansOrder + " | " + this.ansNumber + " | " +this.ansValue
  }



/**
 * Compare two QryRow objects by idAnswer property.
 * @param row, a QryRow object to compare with this one
 * @return -1 if this row.idAnswer < idAnswer, +1 if row.idAnswer > idAnswer;
 * 0 if they're equals
 */
  public int compareIdAns (QryRow row) {
    if (row.getIdAnswer() < this.idAnswer)
      return 1
    else if (row.getIdAnswer() > this.idAnswer)
      return -1
    else
      return 0;
  }

} // EO QryRow class