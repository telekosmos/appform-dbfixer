package test.java;

import org.inb.dbtask.*;
import java.util.*;

public class ChangeSubjectCodeTest {
  public static void main (String[] args) {
    System.out.println("Performing bulk change (from file)");
    testMapFile();

    System.out.println("Se finé");
  }


  private static void testSingleChange () {
    Map subject = new HashMap();
    boolean simulation = false;
    subject.put("157012147", "157012086"); // normal
    subject.put("157012033", "157012021"); // 033 has samples
    subject.put("157012057", "157012122"); // swapping
    String dbUrl = "jdbc:postgresql://localhost:5432/appform";
    FixingTasksHub fth = new FixingTasksHub(dbUrl, "gcomesana", "appform");
    Map taskResult = (Map)fth.changeSubjecsCode("localhost", simulation, subject);

    Map patsWithSamples = (Map)taskResult.get("pats_with_samples");
    Map patsNonExistent = (Map)taskResult.get("patients_nonexistent");
    Integer rowsAffected = (Integer)taskResult.get("rows_affected");
    System.out.println("patsWithSamples: "+patsWithSamples.values().toString());
    System.out.println("patients changed: "+rowsAffected);
  }

  private static void testMapFile () {
    String dbUrl = "jdbc:postgresql://localhost:5432/appform";
    boolean simulation = false;
    FixingTasksHub fth = new FixingTasksHub(dbUrl, "gcomesana", "appform");
    String pathFile = "/Users/telekosmos/DevOps/epiquest/epiquest-admin/" +
      "resources/subject-change-1811.txt";
    Map subjects = fth.getSubjectCodesMapFromFile(pathFile);
    Map taskResult = (Map)fth.changeSubjecsCode("localhost", simulation, subjects);

    Map patsWithSamples = (Map)taskResult.get("pats_with_samples");
    Map patsNonExistent = (Map)taskResult.get("patients_nonexistent");
    Integer rowsAffected = (Integer)taskResult.get("rows_affected");
    System.out.println("patsWithSamples: "+patsWithSamples.values().toString());
    System.out.println("Non existent subjects: "+patsNonExistent.keySet().toString());
    System.out.println("patients changed: "+rowsAffected);


  }
}


