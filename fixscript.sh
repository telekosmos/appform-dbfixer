#!/bin/bash

# Default Values

WHAT_PRM=""
DBHOST_PRM=""
DBNAME_PRM=""
DBUSER_PRM=""
DBPASS_PRM=""
DBPOST_PRM=""
CODES_PRM=""
SIM_PRM=""
HELP_PRM=""

# Scanning inputs

function usageparam()
{
    echo "if this was a real script you would see something useful here"
    echo "(param was $1)"
    echo "./simple_args_parsing.sh"
    echo "\t-h --help"
    echo "\t--environment=$ENVIRONMENT"
    echo "\t--db-path=$DB_PATH"
    echo ""
}

while [ "$1" != "" ]; do
    PARAM=`echo $1 | awk -F= '{print $1}'`
#    VALUE=`echo $1 | awk -F= '{print $2}'`
		VALUE=`echo $2 | awk -F= '{print $1}'`
    case $PARAM in
      -help)
          HELP_PRM="-help"
          exit
          ;;
    	-what) # echo "value $VALUE"
    			WHAT_PRM="-what $VALUE"
    			;;
      -codes)
          CODES_PRM="-codes $VALUE"
          ;;
      -dbhost)
          DBHOST_PRM="-dbhost $VALUE"
          ;;
		  -dbname)
          DBNAME_PRM="-dbname $VALUE"
          ;;
      -dbport)
          DBPORT_PRM="-dbport $VALUE"
          ;;
      -dbpasswd)
          DBPASS_PRM="-dbpasswd $VALUE"
          ;;
      -dbuser)
          DBUSER_PRM="-dbuser $VALUE"
          ;;
      -sim)
					SIM_PRM="-sim $VALUE"
					;;
        *)
          echo "ERROR: unknown parameter \"$PARAM\""
          exit 1
          ;;
    esac
    shift
		shift # coge de 2 en 2
done
echo "Loop done"

# echo "Args: $* \nvara: $vara \noptfile: $varb \nvarbname: ${varbname} \nvarc: $varc"
# echo "Got: $WHAT_PRM"

# FIX_CLASSPATH=lib/postgresql-8.3-603.jdbc3.jar
# GROOVY_PATH=lib/groovy-1.8.6.jar:lib/commons-cli-1.2.jar
# FULL_PATH=${FIX_CLASSPAH}:${GROOVY_PATH}

# -what answers -dbhost gredos -dbname appform -dbuser gcomesana -dbpasswd appform -dbport 5432
echo "java -jar FixScript.jar $WHAT_PRM $CODES_PRM $DBHOST_PRM $DBNAME_PRM $DBUSER_PRM $DBPASS_PRM $DBPORT_PRM $SIM_PRM"
java -jar FixScript.jar $HELP_PRM $WHAT_PRM $CODES_PRM $DBHOST_PRM $DBNAME_PRM $DBUSER_PRM $DBPASS_PRM $DBPORT_PRM $SIM_PRM
