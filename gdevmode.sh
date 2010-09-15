#!/bin/sh

GWT_UTILS_HOME=$HOME/projects/gwt-utils

CLASSPATH=`echo gdevmode/lib_managed/scala_2.8.0/compile/gwt-dev-*.jar \
    environ/lib_managed/scala_2.8.0/compile/gwt-user-*.jar \
    environ/lib_managed/scala_2.8.0/compile/samskivert-*.jar \
    $GWT_UTILS_HOME/target/classes \
    $GWT_UTILS_HOME/src/main/java \
    environ/src/main/resources \
    environ/src/main/java | sed 's/ /:/g'`

java -classpath $CLASSPATH -Xmx256M \
    com.google.gwt.dev.DevMode \
    -noserver -port 8080 -startupUrl /coreen/index.html \
    -war environ/target/scala_2.8.0/war coreen
