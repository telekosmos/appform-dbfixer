#!/bin/bash

JAR_FILES=~/Development/commons-lib/appform-lib/postgresql-8.1-411.jdbc3.jar:./SqlRunner.jar:.

groovy -classpath ${JAR_FILES} src/FixAnswersScript.groovy sim 0