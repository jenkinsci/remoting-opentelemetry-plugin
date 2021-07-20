#!/bin/bash

echo_and_exec()
{
  echo $@
  eval $@
}

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
  esac
done

if [ -z "$WORK_DIR" ] || [ -z "$NODE_NAME" ]
then
  help
fi

shift
shift
shift
shift

curl $JENKINS_ROOT/jnlpJars/agent.jar -o agent.jar
echo_and_exec java \
  -Djava.awt.headless=true -jar agent.jar -jnlpUrl $JENKINS_ROOT/computer/$NODE_NAME/slave-agent.jnlp \
  -workDir $WORK_DIR $*
