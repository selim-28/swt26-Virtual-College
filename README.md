# Template Repository for the Software Engineering Lecture
test
> [!IMPORTANT]
> Replace this README with a detailed description of your project, your team, your architecture decisions, and instructions on how to run it.

> [!IMPORTANT]
> If you keep your repository private, invite all team members and teaching staff.

This template is set up as a multi-module Maven repository with Spring Boot `4.0.6` on Java `21`. GitHub Actions builds the repository from the root `pom.xml`, so you do not need to edit workflow files when you rename or add Maven modules.

## Team

Team Leader: [Name]

Members: [Name1], [Name2], [Name3]

## Quickstart

Run all Maven modules from the repository root:

```bash
./mvnw verify
./mvnw -pl app spring-boot:run
```

Open the starter application endpoints:

```text
http://localhost:8080/api/hello
http://localhost:8080/actuator/health
```

Build the default container image from the repository root:

```bash
docker build -f Dockerfile.java --build-arg MODULE=app .
```

## Prerequisites

- Java 21
- No local Maven installation required because the repository includes the Maven Wrapper
- Docker (optional, only if you build container images)

## Installation and Setup

1. Clone the repository.
2. Open the repository root as a Maven project in your IDE.
3. Use the existing issue templates to capture user stories and bug reports.
4. Replace the sample `app` module with your own implementation.
5. Update `groupId`, `artifactId`, package names, and configuration files to match your project.

## Running the Project

Run all modules and tests:

```bash
./mvnw verify
```

Run the default Spring Boot application module:

```bash
./mvnw -pl app spring-boot:run
```

Package only the default application module:

```bash
./mvnw -pl app -am package
java -jar app/target/app-1.0.0-SNAPSHOT.jar
```

## Project Structure

```text
ProjectName/
├── pom.xml                     # Parent and aggregator Maven project
├── app/                        # Starter application module
│   ├── pom.xml
│   └── src/
├── Dockerfile.java             # Container build for a selected Maven module
├── .github/ISSUE_TEMPLATE/     # Planning and bug-report templates
├── .github/PULL_REQUEST_TEMPLATE.md
└── .github/workflows/          # CI and release automation
```

## Software Development Lifecycle

Use the template as the foundation for the full project lifecycle:

1. Capture work with the GitHub issue templates.
2. Discuss scope, acceptance criteria, and responsibilities in the issue.
3. Implement changes on a branch and keep the repository build green with `./mvnw verify`.
4. Open a pull request using the PR template and address review feedback.
5. Merge into `main` only after CI passes for both the Maven build and deployable container builds.
6. Let the release workflow package and publish deployable modules that opt into container publishing.

## Adding More Modules

1. Create a new module directory with its own `pom.xml`.
2. Add the module to the root `pom.xml` under `<modules>`.
3. Run `./mvnw verify` from the repository root.
4. If the module is a deployable service, make sure it produces a runnable Spring Boot jar.

If a module should be published as a container image by `release.yaml`, add this property to that module's `pom.xml`:

```xml
<properties>
    <container.image.enabled>true</container.image.enabled>
</properties>
```

The release workflow discovers deployable modules from Maven metadata, so students do not need to maintain project-folder lists in GitHub Actions.
