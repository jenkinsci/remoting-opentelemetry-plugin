#!/bin/sh

if test -f /opt/jenkins/agent.jar; then
  echo "will use given agent.jar"
else
  while :
  do
    curl --fail $JENKINS_ROOT/jnlpJars/agent.jar -o agent.jar
    if [ $? -eq 0 ]; then
      break
    fi
    sleep 1
  done
fi

MONITORING_ENGINE_FLAG=""
if test -f /opt/jenkins/monitoring-engine.jar; then
  MONITORING_ENGINE_FLAG="-cp /opt/jenkins/monitoring-engine.jar"
fi

ENGINE_INST_NAME="io.jenkins.plugins.remotingopentelemetry.engine.listener.RemoteEngineInstrumentationListener"
LAUNCHER_INST_NAME="io.jenkins.plugins.remotingopentelemetry.engine.listener.RemoteLauncherInstrumentationListener"

java -Dhudson.remoting.Engine.engineInstrumentationListenerCanonicalNames=$ENGINE_INST_NAME \
 -Dhudson.remoting.Launcher.launcherInstrumentationListenerCanonicalNames=$LAUNCHER_INST_NAME \
  -jar agent.jar -jnlpUrl $JENKINS_ROOT/computer/$NODE_NAME/jenkins-agent.jnlp \
  -workDir $WORK_DIR $MONITORING_ENGINE_FLAG
