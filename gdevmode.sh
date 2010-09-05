#!/bin/sh

CLASSPATH=`echo util/lib_managed/scala_2.8.0/compile/* \
    environ/lib_managed/scala_2.8.0/compile/* \
    project/boot/scala-2.8.0/lib/scala-library.jar \
    project/plugins/lib_managed/scala_2.7.7/gwt-dev-2.0.4.jar \
    environ/src/main/resources \
    environ/src/main/java | sed 's/ /:/g'`

java -classpath $CLASSPATH -Xmx256M \
    com.google.gwt.dev.DevMode \
    -noserver -port 8080 -startupUrl index.html \
    -war target/scala_2.8.0/gwt coreen
