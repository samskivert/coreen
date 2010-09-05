#!/bin/sh

SERVICE_SRCS=`find environ/src/main/java -name '*Service.java'`
java \
  -classpath "project/plugins/lib_managed/scala_2.7.7/*":environ/target/scala_2.8.0/classes \
  com.samskivert.asyncgen.AsyncGenTool \
  $SERVICE_SRCS
