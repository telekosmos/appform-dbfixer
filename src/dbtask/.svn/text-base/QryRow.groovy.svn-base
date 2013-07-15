package dbtask

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

import java.sql.Timestamp

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
  Timestamp lastUpdated



  public QryRow (Integer idQue, Integer idPat, Integer ansNum, Integer ansOrd,
                Integer idAns, String ansVal) {
    this.idQuestion = idQue;
    this.idPat = idPat
    this.ansNumber = ansNum
    this.ansOrder = ansOrd
    this.idAnswer = idAns
    this.ansValue = ansVal
  }


  public QryRow (Integer idQue, Integer idPat, Integer ansNum, Integer ansOrd,
                Integer idAns, String ansVal, Timestamp createDate, Timestamp updDate) {
    this.idQuestion = idQue;
    this.idPat = idPat
    this.ansNumber = ansNum
    this.ansOrder = ansOrd
    this.idAnswer = idAns
    this.ansValue = ansVal

    this.dateCreated = createDate
    this.lastUpdated = updDate
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


  public Timestamp getDateCreated () {
    return this.dateCreated
  }


  public Timestamp getLastUpdate () {
    return this.lastUpdated
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





/**
 * Compare the creation date of this row with the creation date of the
 * row passed as the paremeter
 * @param row, an QryRow object to compare the creation date
 * @return 0, the creation dates are the same; < 0 this creation date is lower
 * than row creation date; > 0 if date is bigger than row creation date
 */
  public int compareCreationDate (QryRow row) {
    Timestamp rowCreated = row.getDateCreated()

    return dateCreated.compareTo(rowCreated);
  }

} // EO QryRow class
