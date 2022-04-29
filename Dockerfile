FROM containerregistry.disney.com/digital/identity-base-jvm17:latest

VOLUME /tmp

ARG JAR_FILE=book-*.jar

HEALTHCHECK \
    CMD curl -fail http://localhost:8080/actuator/health  || exit 1

EXPOSE 8080

USER appuser

COPY --chown=appuser:appuser target/${JAR_FILE} ${JAR_FILE} app.jar

ENV JAVA_OPTS=
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
