#!/bin/bash -x

FUNNELBACK_SHARED_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.1:evaluate -Dexpression=project.version -q -DforceStdout)
echo $FUNNELBACK_SHARED_VERSION

if [[ $FUNNELBACK_SHARED_VERSION == *"SNAPSHOT" ]]
then
  echo "Specified funnelback-shared version is : " $FUNNELBACK_SHARED_VERSION
  echo "You need to set the version to not contain SNAPSHOT when sending to maven central, check the README for more details"
  exit 1
fi
exit 0