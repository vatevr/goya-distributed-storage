FROM java:8

WORKDIR /

ADD ./target/manager-1.0-SNAPSHOT-jar-with-dependencies.jar /manager.jar

ENTRYPOINT ["java", "-jar", "manager.jar"]
