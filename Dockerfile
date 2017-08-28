FROM openjdk:8-jdk-alpine

ARG root=.
ARG jar=target/artifactory-resource.jar

COPY ${root}/assets/ /opt/resource/
COPY ${jar} /artifact/artifactory-resource.jar

RUN chmod +x /opt/resource/check /opt/resource/in /opt/resource/out