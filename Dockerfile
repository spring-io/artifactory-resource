FROM ubuntu:jammy-20240111

ARG root=.
ARG jar=build/libs/artifactory-resource.jar

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
    curl -L https://github.com/bell-sw/Liberica/releases/download/17.0.10+13/bellsoft-jdk17.0.10+13-linux-amd64.tar.gz | tar xz --strip-components=1

RUN chmod +x /opt/resource/check /opt/resource/in /opt/resource/out
