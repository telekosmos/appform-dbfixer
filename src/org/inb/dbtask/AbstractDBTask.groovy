package org.inb.dbtask
/**
 * Created by IntelliJ IDEA.
 * User: bioinfo
 * Date: Aug 31, 2010
 * Time: 6:25:16 PM
 * To change this template use File | Settings | File Templates.
 */

import groovy.sql.Sql
import java.sql.ResultSet

public abstract class AbstractDBTask {

  protected Sql theSql;
  protected String qry;


/**
 * Initializes the Sql object to perform queries for the task
 * @param dbUrl, the url connection for the database, currently only postgresql supported
 * @param dbUser, the database username
 * @param dbPasswd, the password
 * @return a Sql object ready to use to query the database
 */
  public Sql initSql (String dbUrl, String dbUser, String dbPasswd) {
    theSql = Sql.newInstance(dbUrl, dbUser, dbPasswd, 'org.postgresql.Driver')

    theSql
  }



  
  public void setQuery (String strQry) {
    qry = strQry.toString();
  }




/**
 * Performa a task over the rs content
 * @param rs, the resultset as got from a query
 */
  public abstract Object performTask (Sql sql, Closure clos);

}