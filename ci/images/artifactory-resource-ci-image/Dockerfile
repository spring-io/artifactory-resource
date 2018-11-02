FROM openjdk:8u141-jdk

RUN apt-get update && \
    apt-get -y install net-tools && \
    apt-get -y install libxml2-utils && \
    apt-get install -y git

ADD https://raw.githubusercontent.com/spring-io/concourse-java-scripts/v0.0.2/concourse-java.sh /opt/
