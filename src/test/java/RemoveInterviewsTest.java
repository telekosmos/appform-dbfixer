package test.java;


import java.util.*;

import groovy.sql.GroovyResultSet;
import org.inb.dbtask.*;
import org.inb.util.*;

public class RemoveInterviewsTest {

  public static void main (String[] args) {

    String dbUrl = "jdbc:postgresql://localhost:5432/appform";
    FixingTasksHub fs = new FixingTasksHub(dbUrl, "gcomesana", "appform");
    boolean simulation = true;
    DBQuery dbq = new DBQuery(dbUrl, "gcomesana", "appform");

    String patCode = "TER521172";
    patCode = "157071005";
    String listIntrvs = "Inform Consent Kids,Questionnaire_EPI_YOUNG";
    listIntrvs = "QES_Español, Formulario_participacion";

    getNumOfIntrvs(dbq, patCode);
    HashMap hashMap = new HashMap();
    String[] intrvs = listIntrvs.split(",");
    List<String> theIntrvs = Arrays.asList(intrvs);
    hashMap.put(patCode, theIntrvs);

    HashMap<String, Object> jsonMap =
      (HashMap<String, Object>)fs.deleteInterviews("localhost",
        "gcomesana", "appform", simulation, hashMap);

    // System.out.println("last interviews for "+patCode+": "+jsonMap.get("last_interview").toString());
    System.out.println(delIntrvs2Json(jsonMap));
    System.out.println("Finito");
  }


  private static String delIntrvs2Json (HashMap jsonMap) {
    String jsonOut = "{\"rows_affected\": "+jsonMap.get("rows_affected").toString()+",";
    jsonOut += "\"samples\": [";

    HashMap patSamples = (HashMap)jsonMap.get("pats_with_samples");
    List samples = new ArrayList();
    samples.addAll(patSamples.values());
    Iterator sampleIt = samples.iterator();
    while (sampleIt.hasNext()) {
      jsonOut += sampleIt.next().toString()+",";
    }
    jsonOut = samples.size()>0? jsonOut.substring(0, jsonOut.length()-1): jsonOut;

    jsonOut += "], \"interviews_deleted\":[";
    List deletedOnes = (List)jsonMap.get("interviews_deleted");
    Iterator deletedIt = deletedOnes.iterator();
    while (deletedIt.hasNext()) {
      // jsonOut += "\""+deletedIt.next().toString()+"\",";
      List pair = (List)deletedIt.next();
      jsonOut += "{\"codpat\":\""+pair.get(0)+"\", \"intrv\":\""+pair.get(1)+"\"},";
    }
    jsonOut = deletedOnes.size()>0? jsonOut.substring(0, jsonOut.length()-1):jsonOut;

    jsonOut += "], \"last_interviews\":[";
    LinkedHashSet lastIntrvs = (LinkedHashSet)jsonMap.get("last_interviews");
    // List lastOnes = new ArrayList();
    // lastOnes.addAll(lastIntrvs.values()) ;
    Iterator lastOne = lastIntrvs.iterator();
    while(lastOne.hasNext()) {
      List pair = (List)lastOne.next();
      jsonOut += "{\"codpat\":\""+pair.get(0)+"\", \"last_one\":\""+pair.get(1)+"\"},";
      // jsonOut += lastOne.next().toString()+",";
    }
    jsonOut = lastIntrvs.size()>0? jsonOut.substring(0, jsonOut.length()-1): jsonOut;

    jsonOut += "], \"sim\":"+true+"}";

    return jsonOut;
  }

  private static void getNumOfIntrvs (DBQuery dbq, String codPat) {
    long counter = (Long)dbq.getNumIntrvs4Pat(codPat);
    // System.out.println("Num of rows: "+counter);
    System.out.println("Num of interviews for "+codPat+": "+counter);
  }
}
