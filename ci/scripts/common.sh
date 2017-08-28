source /opt/concourse-java.sh

build() {
	run_maven clean install -Prun-local-artifactory
}

setup_symlinks
cleanup_maven_repo "io.spring.concourse.artifactoryresource"
