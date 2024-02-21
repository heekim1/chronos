#!/usr/bin/env bash

set -x
cd /chronos
apt-get update
apt-get install -y maven openjdk-8-jdk
update-java-alternatives --jre-headless -s java-1.8.0-openjdk-amd64
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64

mvn -U clean install -Dpack -Dmaven.test.skip
#mvn clean test -Dskip.bdd.tests=true -Dskip.unit.tests=false -Dskip.integration.tests=true

chown -R 5000:$GROUPID target/ 
chmod -R 775 target/ 
cp target/chronos.jar /bfx/bin/${NAME}/${VERSION}
chown -R 5000:$GROUPID /bfx/
chmod -R 775 /bfx/
exit 0
