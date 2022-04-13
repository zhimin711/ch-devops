FROM java:openjdk-8-alpine
VOLUME /tmp
ADD ch-mq-*-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS="-Dnacos.config.server-addr=nacos.12301.io:80 -Dnacos.config.namespace=cloud-dev"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
