FROM ubuntu:jammy-20240111

ADD setup.sh /setup.sh
ADD get-docker-url.sh /get-docker-url.sh
RUN ./setup.sh

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH
ADD docker-lib.sh /docker-lib.sh
