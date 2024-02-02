#!/bin/bash
set -e

source $(dirname $0)/common.sh

git clone git-repo release-git-repo

pushd release-git-repo > /dev/null
snapshotVersion=$( awk -F '=' '$1 == "version" { print $2 }' gradle.properties )
releaseVersion=$( get_next_release $snapshotVersion)
nextVersion=$( bump_version_number $snapshotVersion)
echo "Releasing $releaseVersion (next version will be $nextVersion)"
sed -i "s/version=$snapshotVersion/version=$releaseVersion/" gradle.properties
sed -i 's/\(artifactory-resource.*tag\:\ \).*\(\}\)/\1${releaseVersion}\2/' samples/simple/pipeline.yml > /dev/null
git config user.name "Spring Builds" > /dev/null
git config user.email "spring-builds@users.noreply.github.com" > /dev/null
git add gradle.properties > /dev/null
git commit -m"Release v$releaseVersion" > /dev/null
git tag -a "v$releaseVersion" -m"Release v$releaseVersion" > /dev/null
build
echo "Setting next development version (v$nextVersion)"
git reset --hard HEAD^ > /dev/null
sed -i "s/version=$snapshotVersion/version=$nextVersion/" gradle.properties
sed -i 's/\(artifactory-resource.*tag\:\ \).*\(\}\)/\1${nextVersion}\2/' samples/simple/pipeline.yml > /dev/null
sed -i 's/\(\:artifactory-resource-release-version\:\ \).*/\1${releaseVersion}/' README.adoc > /dev/null
sed -i 's/\(\:artifactory-resource-snapshot-version\:\ \).*/\1${nextVersion}/' README.adoc > /dev/null

git add gradle.properties > /dev/null
git add README.adoc > /dev/null
git add samples/simple/pipeline.yml > /dev/null
git commit -m"Next development version (v$nextVersion)" > /dev/null
popd > /dev/null

cp release-git-repo/target/artifactory-resource.jar built-artifact/
echo $releaseVersion > built-artifact/version
