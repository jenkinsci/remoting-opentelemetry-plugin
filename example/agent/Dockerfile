FROM jenkins/agent:jdk11

ARG ENGINE_LOCATION="https://repo.jenkins-ci.org/artifactory/releases/io/jenkins/plugins/remoting-opentelemetry-engine/\[RELEASE\]/remoting-opentelemetry-engine-\[RELEASE\].jar"
ARG user=jenkins
USER root
RUN apt update && apt install curl
COPY jenkins-agent /usr/local/bin/jenkins-agent
COPY logging.properties /usr/share/remoting-otel/logging.properties
RUN chmod +x /usr/local/bin/jenkins-agent &&\
    mkdir -p /usr/share/remoting-otel/monitoring-engine.jar &&\
    curl $ENGINE_LOCATION -o /usr/share/remoting-otel/engine.jar

USER $user
ENTRYPOINT ["/usr/local/bin/jenkins-agent"]
