# Receipt-Processor-Challenge

Take-home backend challenge for https://github.com/fetch-rewards

See [docs](docs) for the [original prompt](docs/OriginalPrompt.md) and [API spec](docs/api.yml),

This challenge was implemented as a REST API with Java 17, Spring Boot, and DynamoDB. However, the app and tests
are able to be run using Docker only.

DynamoDB was chosen as a simple NoSQL database to retrieve based on receipt ID. This can easily be extended to include
other indexes, such as the most recent receipt for each retailer, or sorting by the receipt with the most points. If the
data is persisted elsewhere, then simply adding a time-to-live on
our dynamo documentss or even using a Redis cache would work for our purposes too.

The app itself utilizes the DTO and Command pattern to cleanly award points to each receipt following all the point
rules. And an options object was also included that can be easily extended to customize each rule, or even disable them.

All classes are unit tested with JUnit 5 + Spring Boot testing utilities, and Integration tests are enabled through the
use of testcontainers, see [Running Tests](#running-tests)

## Development Requirements

* Java 17
* Docker & docker-compose
* Preferably IntelliJ IDE
    * Lombok annotations included on IntelliJ, otherwise will need to install plugins for IDE
* Maven 3.9.0, or utilize the maven wrapper (mvnw, or mvnw.cmd for Windows)

## Building and Running Locally

Default port for receipt-processor app are 8080, and DynamoDB-Local on 8000

* http://localhost:8080
* http://localhost:8000

If you receive this error on docker builds, it means your docker containers are not able to connect to the internet
I've found a simple solution is to restart the docker service, or my entire computer to fix this issue.

```
Could not transfer artifact * from/to central (https://repo.maven.apache.org/maven2)
```

### Run and Build With Docker Compose

This will build the spec defined in docker-compose.yml, pulling in the latest src/ changes for the app and deploying in
a docker-compose environment along with DynamoDB-Local

```
docker-compose up --build
```

You can alternatively just build the docker image with

```
docker build -t receipt-processor:latest .
```

### Build and Run Manually

```
# Build app
mvn package -DskipTests

# Run dynamodb-local
docker run -p 8000:8000 amazon/dynamodb-local

# Run app
java -jar target/receipt-processor-0.0.1.jar
```

# Running Tests

## Run Unit + Integration Tests with Docker!:

This method does not require maven or a Java installation on your system.

The following should be run from the root of the project directory and will execute on a docker container running maven:

```
docker run -it --rm -v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock maven:3.9.0-eclipse-temurin-17 mvn test
```

See testcontainers documentation here for more information:
https://www.testcontainers.org/supported_docker_environment/continuous_integration/dind_patterns/
> -v $PWD:$PWD will add your current directory as a volume inside the container \
> -w $PWD will set the current directory to this volume \
> -v /var/run/docker.sock:/var/run/docker.sock will map the Docker socket \
> -it Creates interactive bash shell \
> --rm Automatically remove the container when it exits

## Run tests manually

Run unit and integration tests

```
# With maven installed
mvn test

# Linux/mac without maven installed
./mvnw test

# Windows without maven installed
./mvnw.cmd test
```

Run only unit tests:

```
mvn test -Dtest="*Unit*"
```

Run only integration tests

```
mvn test -Dtest="*Integration*"
```