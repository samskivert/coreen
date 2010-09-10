#!/bin/sh

CLASSPATH=`echo gdevmode/lib_managed/scala_2.8.0/compile/gwt-dev-2.0.4.jar \
    environ/lib_managed/scala_2.8.0/compile/gwt-user-2.0.4.jar \
    environ/lib_managed/scala_2.8.0/compile/samskivert-1.0.jar \
    environ/lib_managed/scala_2.8.0/compile/gwt-utils-1.0-SNAPSHOT.jar \
    environ/src/main/resources \
    environ/src/main/java | sed 's/ /:/g'`

java -classpath $CLASSPATH -Xmx256M \
    com.google.gwt.dev.DevMode \
    -noserver -port 8080 -startupUrl /coreen/index.html \
    -war environ/target/scala_2.8.0/resources coreen
