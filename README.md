# receipt-processor-challenge

Take-home backend challenge for https://github.com/fetch-rewards

# Build and run app with Docker

Running will build the service, package, and run on docker containers locally.

Receipt processor running on: http://localhost:8080 and DynamoDB-local on: http://localhost:8000

```
docker-compose up --build
```

# Run unit + integration tests with Docker!:

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

