FROM java:8

WORKDIR /

ADD ./target/node-1.0-SNAPSHOT-jar-with-dependencies.jar /node.jar

ENTRYPOINT ["java", "-jar", "node.jar"]
