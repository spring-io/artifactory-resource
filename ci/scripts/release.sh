#!/bin/bash
set -e

source $(dirname $0)/common.sh

git clone git-repo release-git-repo

pushd release-git-repo > /dev/null
snapshotVersion=$( get_revision_from_pom )
releaseVersion=$( strip_snapshot_suffix "$snapshotVersion" )
nextVersion=$( bump_version_number "$snapshotVersion" )
echo "Releasing $releaseVersion (next version will be $nextVersion)"
set_revision_to_pom "$releaseVersion"
sed -i 's/\(artifactory-resource.*tag\:\ \).*\(\}\)/\1${releaseVersion}\2/' samples/simple/pipeline.yml > /dev/null
git config user.name "Spring Buildmaster" > /dev/null
git config user.email "buildmaster@springframework.org" > /dev/null
git add pom.xml > /dev/null
git commit -m"Release v$releaseVersion" > /dev/null
git tag -a "v$releaseVersion" -m"Release v$releaseVersion" > /dev/null
build
echo "Setting next development version (v$nextVersion)"
git reset --hard HEAD^ > /dev/null
set_revision_to_pom "$nextVersion"
sed -i 's/\(artifactory-resource.*tag\:\ \).*\(\}\)/\1${nextVersion}\2/' samples/simple/pipeline.yml > /dev/null
sed -i 's/\(\:artifactory-resource-release-version\:\ \).*/\1${releaseVersion}/' README.adoc > /dev/null
sed -i 's/\(\:artifactory-resource-snapshot-version\:\ \).*/\1${nextVersion}/' README.adoc > /dev/null

git add pom.xml > /dev/null
git commit -m"Next development version (v$nextVersion)" > /dev/null
popd > /dev/null

cp release-git-repo/target/artifactory-resource.jar built-artifact/
echo $releaseVersion > built-artifact/version
