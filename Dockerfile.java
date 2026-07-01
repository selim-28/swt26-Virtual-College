FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

ARG MODULE=app

COPY . .

# Build the selected Maven module together with any modules it depends on.
RUN mvn --batch-mode -pl "${MODULE}" -am -DskipTests package \
    && JAR_NAME="$(mvn --batch-mode -q -pl "${MODULE}" -DforceStdout help:evaluate -Dexpression=project.build.finalName).jar" \
    && JAR_PATH="${MODULE}/target/${JAR_NAME}" \
    && test -f "${JAR_PATH}" \
    && cp "${JAR_PATH}" /tmp/app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /tmp/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
