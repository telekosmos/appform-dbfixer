import util.DBQuery;
import dbtask.FixingTasksHub;

import java.lang.*;

/*
import java.lang.Integer;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;
*/
import java.lang.System;
import java.util.*;
/*
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
*/

public class TestSqlRunnerJar {

  private static String DEFAULT_DB_URL = "jdbc:postgresql://localhost:4321/appform"; // 170613";
  private static String DEFAULT_DB_USR = "gcomesana";
  private static String DEFAULT_DB_PASS = "appform";
  private static String DEFAULT_DB_HOST = "localhost";

  public static void main(String argv[]) {
    boolean simulation = true;
    // RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask(simulation);

    TestSqlRunnerJar tsj = new TestSqlRunnerJar();

    String dbUrl = TestSqlRunnerJar.DEFAULT_DB_URL;
    String dbUser = TestSqlRunnerJar.DEFAULT_DB_USR;
    String dbPass = TestSqlRunnerJar.DEFAULT_DB_PASS;

    // fs.curateAnswers(dbUrl, dbUser, dbPass, simulation);
    // System.out.println("dbtask.FixTasks: Se fine...");

    System.out.println("Starting test...");
    DBQuery dbq = new DBQuery(TestSqlRunnerJar.DEFAULT_DB_URL,
      TestSqlRunnerJar.DEFAULT_DB_USR, TestSqlRunnerJar.DEFAULT_DB_PASS);

    try {
      System.out.println("Test 1: DELETE PATIENTS");
      tsj.delPatientsTest(dbq);

      System.out.println("Test 2: DELETE INTERVIEWS");
      tsj.delInterviews();
    }
    catch (java.lang.Exception ex) {
      ex.printStackTrace();
    }



  }


  public void delInterviews () throws java.io.UnsupportedEncodingException {
    HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
    String qes = new String("QES_Español".getBytes("ISO-8859-1"));
    qes = "QES_Español";
    System.out.print("\n"+qes+"-> bytes: ");
/*
    byte resqes[] = qes.getBytes("ISO-8859-1");
    for (int i=0; i<resqes.length; i++)
      System.out.print(resqes[i]+", ");

    System.out.println("\n");
*/

    hashMap.put("157071001", new ArrayList<String>(Arrays.asList("Dieta")));
    hashMap.put("157071004", new ArrayList<String>(Arrays.asList("Dieta")));
    // hashMap.put("157071005", new ArrayList<String>(Arrays.asList(qes, "Dieta", "Calidad_Entrevista")));
    hashMap.put("157071026", new ArrayList<String>(Arrays.asList("Dieta")));
    hashMap.put("157071027", new ArrayList<String>(Arrays.asList("Dieta")));
    // hashMap.put("157071068", new ArrayList<String>(Arrays.asList(qes)));

    hashMap.put("157072023", new ArrayList<String>(Arrays.asList("Calidad_Entrevista")));
    hashMap.put("157072025", new ArrayList<String>(Arrays.asList("Calidad_Entrevista")));
    // hashMap.put("157072050", new ArrayList<String>(Arrays.asList(qes)));
    // hashMap.put("157072202", new ArrayList<String>(Arrays.asList(qes)));

    FixingTasksHub fs = new FixingTasksHub();
    HashMap<String, Object> jsonMap =
      (HashMap<String, Object>)fs.deleteInterviews(TestSqlRunnerJar.DEFAULT_DB_HOST,
                    TestSqlRunnerJar.DEFAULT_DB_USR, TestSqlRunnerJar.DEFAULT_DB_PASS,
                  false, hashMap);

    Iterator it = jsonMap.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry)it.next();
      System.out.println("** "+pairs.getKey() + " => " + pairs.getValue());
      it.remove(); // avoids a ConcurrentModificationException
    }
  }




  public void delPatientsTest(DBQuery dbq) {
    System.out.println("delPatientsTest method running...");

    FixingTasksHub fs = new FixingTasksHub();
    LinkedHashMap<Integer, String> res =
      (LinkedHashMap<Integer, String>) dbq.getSamples4Patient("188011009");

    Set<Integer> enKeys = res.keySet();

    System.out.println("Samples for 188011009:");
    for (Iterator<Integer> it = enKeys.iterator(); it.hasNext(); ) {
      Integer key = it.next();
      System.out.println(key + ":" + res.get(key));
    }

    ArrayList<String> patients = new ArrayList<String>();
    patients.add("188011009");
    patients.add("157102002");
    /*
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("patients", patients);
        map.put("sim", true);
    */
    Integer deletions = fs.deletePatients(TestSqlRunnerJar.DEFAULT_DB_HOST,
      TestSqlRunnerJar.DEFAULT_DB_USR,
      TestSqlRunnerJar.DEFAULT_DB_PASS,
      true, patients);

    ArrayList<String> noDeletions = new ArrayList<String>();
    noDeletions = fs.getNoDeletedPatients();
  }



  public void changePatientCodes () {
    System.out.println("Change patient codes... in progress");
  }

}