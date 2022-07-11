FROM java:openjdk-8-alpine
VOLUME /tmp
ADD apache-skywalking-java-agent-8.11.0.tgz .
RUN sh -c 'mv skywalking-agent agent'
ADD ch-devops-*-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENV AGENT_OPTS="-javaagent:/agent/skywalking-agent.jar"

EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $AGENT_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
