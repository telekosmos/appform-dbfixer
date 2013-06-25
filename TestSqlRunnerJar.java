

import util.DBQuery;
import dbtask.FixTasks;

import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class TestSqlRunnerJar {

  private static String DEFAULT_DB_URL = "jdbc:postgresql://localhost:4321/appform170613";
  private static String DEFAULT_DB_USR = "gcomesana";
  private static String DEFAULT_DB_PASS = "appform";

	public static void main(String argv[]) {
		boolean simulation = true;
		// RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask(simulation);

    FixTasks fs = new FixTasks();
    String dbUrl = TestSqlRunnerJar.DEFAULT_DB_URL;
    String dbUser = TestSqlRunnerJar.DEFAULT_DB_USR;
    String dbPass = TestSqlRunnerJar.DEFAULT_DB_PASS;

    // fs.curateAnswers(dbUrl, dbUser, dbPass, simulation);
    // System.out.println("dbtask.FixTasks: Se finé...");

    System.out.println("Starting test...");
    DBQuery dbq = new DBQuery(TestSqlRunnerJar.DEFAULT_DB_URL,
        TestSqlRunnerJar.DEFAULT_DB_USR, TestSqlRunnerJar.DEFAULT_DB_PASS);

    LinkedHashMap<Integer, String> res =
      (LinkedHashMap<Integer, String>)dbq.getSamples4Patient("188011009");

    Set<Integer> enKeys = res.keySet();

    System.out.println("Samples for 188011009:");
    for (Iterator<Integer> it = enKeys.iterator(); it.hasNext();) {
      Integer key = it.next();
      System.out.println(key+":"+res.get(key));
    }

    ArrayList<String> patients = new ArrayList<String>();
    patients.add("188011009");
    patients.add("157102002");
/*
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("patients", patients);
    map.put("sim", true);
*/
    Integer deletions = fs.deletePatients(TestSqlRunnerJar.DEFAULT_DB_URL, TestSqlRunnerJar.DEFAULT_DB_USR,
                      TestSqlRunnerJar.DEFAULT_DB_PASS, true, patients);

    ArrayList<String> noDeletions = new ArrayList<String>();
    noDeletions = fs.getNoDeletedPatients();

	}




}