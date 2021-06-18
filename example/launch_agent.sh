#!/bin/bash

help()
{
  echo "Usage: $0 -w workdir -n node-name"
  exit 1
}

JENKINS_ROOT="${JENKINS_ROOT:-http://localhost:8080/jenkins}"

while getopts w:n: opt
do
  case "$opt" in
    w ) WORK_DIR="$OPTARG" ;;
    n ) NODE_NAME="$OPTARG" ;;
    ? ) help ;;
  esac
done

if [ -z "$WORK_DIR" ] || [ -z "$NODE_NAME" ]
then
  help
fi

curl $JENKINS_ROOT/jnlpJars/agent.jar -o agent.jar
java \
  -Djava.awt.headless=true -jar agent.jar -jnlpUrl $JENKINS_ROOT/computer/$NODE_NAME/slave-agent.jnlp \
  -workDir $WORK_DIR
