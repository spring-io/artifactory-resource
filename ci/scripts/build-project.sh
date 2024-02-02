#!/bin/bash
set -e

source $(dirname $0)/common.sh

pushd git-repo > /dev/null
version=$( awk -F '=' '$1 == "version" { print $2 }' gradle.properties )
./gradlew -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false --no-daemon build
popd > /dev/null

cp git-repo/build/libs/artifactory-resource.jar built-artifact/
echo $version > built-artifact/version
