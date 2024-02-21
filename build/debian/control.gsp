Package: $name-$version
Version: 1.0.1
Section: base
Priority: optional
Architecture: all
Maintainer: Your Name <you@email.com>
Depends: openjdk-8-jre-headless, ca-certificates-java
Environment:
 PATH=/bfx/bin/$name/$version:\$PATH
 PATH=/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java:\$PATH
 JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre
Description: $name
