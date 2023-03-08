FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /builder

# Cache dependencies to prevent re-download on src changes
COPY ./pom.xml /builder
RUN mvn dependency:go-offline

# Build/package application
COPY ./src /builder/src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine AS prod
COPY --from=builder ./builder/target/receipt-processor-0.0.1.jar /

# Create user & group to prevent container from running as root
RUN addgroup -g 10001 -S non-root && \
	adduser -u 10000 -S receipt-app -G non-root
USER receipt-app:non-root

EXPOSE 8080
ENTRYPOINT ["java","-jar","/receipt-processor-0.0.1.jar"]