FROM openjdk:11-slim

WORKDIR /opt/jenkins

RUN apt-get update
RUN apt-get install curl git -y

ADD docker-entrypoint.sh .

ENTRYPOINT [ "./docker-entrypoint.sh" ]
