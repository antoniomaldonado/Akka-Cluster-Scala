# Akka-Scala

### Akka Http and Streams:
This package contains some examples that show how akka streams are the base for akka HTTP and akka TCP.

### Akka Cluster:
Akka cluster that that creates a configurable amount of master workers to count the words in a file. 
* Create the jar file with the command `sbt assembly`
* To run the cluster. Open four terminals and execute the following commands.

```
java -DPORT=2551 -Dconfig.resource=/seed.conf   -jar target/scala-2.12/wordCount.jar
java -DPORT=2553 -Dconfig.resource=/master.conf -jar target/scala-2.12/wordCount.jar
java -DPORT=2554 -Dconfig.resource=/worker.conf -jar target/scala-2.12/wordCount.jar
java -DPORT=2555 -Dconfig.resource=/worker.conf -jar target/scala-2.12/wordCount.jar
```
Unit test to test the word count and the cluster can be found in the test folder.