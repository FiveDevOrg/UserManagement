# JAVA 17 - amazon corretto
FROM amazoncorretto:11.0.9-alpine
ARG JAR_FILE=deploy/auxby-user-manager.jar
ADD ${JAR_FILE} app.jar
# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]