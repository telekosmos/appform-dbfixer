#!/bin/bash

JAR_FILES=/Users/bioinfo/Development/commons-lib/appform-lib/postgresql-8.1-411.jdbc3.jar:./SqlFixer.jar

JAR_FILES=./SqlFixer.jar:./postgresql-8.1-411.jdbc3.jar
java -classpath ${JAR_FILES} -jar FixAnswersScript.jar sim 1
# groovy -classpath ${JAR_FILES} src/FixAnswersScript.groovy
