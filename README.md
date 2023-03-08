# receipt-processor-challenge

Take-home backend challenge for https://github.com/fetch-rewards

# Build and run app with Docker

Running will build the service, package, and run on docker containers locally.

Receipt processor running on: http://localhost:8080 and DynamoDB-local on: http://localhost:8000

```
docker-compose up --build
```

# Run DynamoDB locally as database:

```
docker run -p 8000:8000 amazon/dynamodb-local:1.21.0
```

