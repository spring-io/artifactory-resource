FROM openjdk:8-jdk-alpine

ARG root=.
ARG jar=target/artifactory-resource.jar

COPY ${root}/assets/ /opt/resource/
COPY ${jar} /artifact/artifactory-resource.jar

# https://github.com/concourse/concourse/issues/2042
RUN unlink  $JAVA_HOME/jre/lib/security/cacerts && \
cp /etc/ssl/certs/java/cacerts $JAVA_HOME/jre/lib/security/cacerts

RUN chmod +x /opt/resource/check /opt/resource/in /opt/resource/out