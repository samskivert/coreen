#!/bin/sh

CLASSPATH=`echo project/plugins/lib_managed/scala_2.7.7/gwt-dev-2.0.4.jar \
    project/plugins/lib_managed/scala_2.7.7/gwt-user-2.0.4.jar \
    environ/src/main/resources \
    environ/src/main/java | sed 's/ /:/g'`

java -classpath $CLASSPATH -Xmx256M \
    com.google.gwt.dev.DevMode \
    -noserver -port 8080 -startupUrl /coreen/index.html \
    -war environ/target/scala_2.8.0/classes coreen
