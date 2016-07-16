/** Jenkins 2.0 Buildfile
 *
 * Master Jenkins 2.0 can be started by typing:
 * docker run -d -p 8090:8080 --name jenkins blacklabelops/jenkins
 *
 * Slave 'jdk8docker' can be started by typing:
 * docker run -d -v /var/run/docker.sock:/var/run/docker.sock --link jenkins:jenkins -e "SWARM_CLIENT_LABELS=docker" blacklabelops/swarm-dockerhost
 **/
node ('jdk8docker') {
  checkout scm
  stage 'Build & Test Images'
  sh 'gradle build fatJar'
  stage 'Build Docker Image'
  sh 'docker build -t blacklabelops/backuprotator .'
}
