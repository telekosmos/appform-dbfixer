/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Sep 1, 2010
 * Time: 11:00:27 AM
 * To change this template use File | Settings | File Templates.
 */

import groovy.sql.Sql

import org.inb.dbtask.RemoveMultAnsByDateDBTask

// STARTS GROOVY SCRIPT /////////////////////////////////////////////////////
String dbUrl = "jdbc:postgresql://localhost:5432/appform"
// dbUrl = "jdbc:postgresql://padme:5432/appform"
// dbUrl = "jdbc:postgresql://ubio.bioinfo.cnio.es:5432/appform"
// dbUrl = "jdbc:postgresql://gredos:5432/appform"
String dbUser = "gcomesana"
String dbPass = "appform";


println "Fixing repetad answers for ${dbUrl}..."
Sql theSqlConn = Sql.newInstance(dbUrl, dbUser, dbPass, 'org.postgresql.Driver')

if (theSqlConn != null) {

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
        order by 1, 2, 3, 4, 7, 8;'''
                                                              
//  ResultSet rs = theSqlConn.executeQuery(qry);

  boolean simulation = true
  if (args) {
    if (args.length == 1) {
      println ("Arguments must be: 'sim 1' or 'sim 0' or nothing. Default is 'sim 1")
      return
    }
    else {
      simulation = args[1].compareToIgnoreCase("0") == 0? false: true;
    }
  }

  if (simulation)
    println "Running database SIMULATION update..."
  else
    println "Running database LIVE update..."
  
  RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask(simulation)
//  RemoveMultAnsDBTask rmvdateTask = new RemoveMultAnsDBTask()

//  rmvdateTask.initSql(dbUrl, dbUser, dbPass)
  rmvdateTask.setQuery (qry);
  rmvdateTask.performTask (theSqlConn, {})

}