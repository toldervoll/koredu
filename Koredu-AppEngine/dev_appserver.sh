#!/bin/bash
# Copyright 2009 Google Inc. All Rights Reserved.
#
# Launches the Development AppServer.  This utility allows developers
# to test a Google App Engine application on their local workstation.
#
[ -z "${DEBUG}" ] || set -x  # trace if $DEBUG env. var. is non-zero
SDK_BIN="/Users/thomas//AppEngine/appengine-java-sdk-1.7.1/bin"
SDK_LIB="$SDK_BIN/../lib"
JAR_FILE="$SDK_LIB/appengine-tools-api.jar"

if [ ! -e "$JAR_FILE" ]; then
    echo "$JAR_FILE not found"
    exit 1
fi

pushd /Users/thomas/Dropbox/thomas/workspace/Koredu-AppEngine/war

java -ea -cp "$JAR_FILE" \
  -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
  -javaagent:$SDK_BIN/../lib/agent/appengine-agent.jar \
  com.google.appengine.tools.development.DevAppServerMain \
  --port=8888 .

popd