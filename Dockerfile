FROM ubuntu:focal-20210119

ARG root=.
ARG jar=target/artifactory-resource.jar

COPY ${root}/assets/ /opt/resource/
COPY ${jar} /artifact/artifactory-resource.jar

RUN export DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install --no-install-recommends -y tzdata ca-certificates curl
RUN ln -fs /usr/share/zoneinfo/UTC /etc/localtime
RUN dpkg-reconfigure --frontend noninteractive tzdata
RUN rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH
RUN mkdir -p /opt/openjdk && \
    cd /opt/openjdk && \
    curl https://java-buildpack.cloudfoundry.org/openjdk/bionic/x86_64/openjdk-1.8.0_192.tar.gz | tar xz

RUN chmod +x /opt/resource/check /opt/resource/in /opt/resource/out
