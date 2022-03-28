FROM containerregistry.disney.com/digital/identity-base-jvm17:latest

ARG JAR_FILE=book-*.jar

EXPOSE 8085

USER appuser

COPY --chown=appuser:appuser target/${JAR_FILE} ${JAR_FILE} app.jar

ENV JAVA_OPTS=
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
