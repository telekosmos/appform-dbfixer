/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Dec 9, 2010
 * Time: 4:43:05 PM
 * To change this template use File | Settings | File Templates.
 */

import dbtask.RemoveMultAnsByDateDBTask
import groovy.sql.Sql

public class SqlFixerMain {

  public static void main (String[] args) {
    String dbUrl = "jdbc:postgresql://padme.cnio.es:5432/appform"
//    dbUrl = "jdbc:postgresql://localhost:5432/appform"
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
            order by 1, 2, 3, 4, 7, 8;'''

    //  ResultSet rs = theSql.executeQuery(qry);

      RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask()
    //  rmvdateTask.initSql(dbUrl, dbUser, dbPass)
      rmvdateTask.setQuery (qry);
      rmvdateTask.performTask (theSql)
    } // EO if
  } // EO main

}