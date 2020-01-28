FROM java:8

WORKDIR /

ADD target/scenarios-1.0-SNAPSHOT-jar-with-dependencies.jar /scenarios.jar
ADD fixtures /scenarios/fixtures

ENTRYPOINT ["java", "-jar", "scenarios.jar"]
