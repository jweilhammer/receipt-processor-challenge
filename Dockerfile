FROM eclipse-temurin:17-jre-alpine AS prod
WORKDIR receipt-app
COPY ./target/receipt-processor-0.0.1.jar /

# Create user & group to prevent container from running as root
RUN addgroup -g 10001 -S non-root && \
	adduser -u 10000 -S receipt-app -G non-root
USER receipt-app:non-root

EXPOSE 8080
ENTRYPOINT ["java","-jar","/receipt-processor-0.0.1.jar"]