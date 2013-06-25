@echo off

rem java -jar ${JAR_PATH}/padme-batchdumper.jar -batch ${PROPS_DIR}/bulks.props
java -jar FixScript.jar -what answers -sim 0 -dbhost gredos -dbname appform -dbport 5432 -dbuser gcomesana -dbpasswd appform
rem  $CODES_PRM $DBHOST_PRM $DBNAME_PRM $DBUSER_PRM $DBPASS_PRM $DBPORT_PRM $SIM_PRM