#!/usr/bin/env bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home
mvn clean  deploy -DskipTests

#$JAVA_HOME/bin/java -Dmaven.multiModuleProjectDirectory=/Users/tommy/git/luban4j "-Dmaven.home=/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3" "-Dclassworlds.conf=/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/m2.conf" "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54515:/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/boot/plexus-classworlds-2.6.0.jar" org.codehaus.classworlds.Launcher -Didea.version2019.1.4  install -DskipTests
