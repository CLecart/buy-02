FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Expect the service jar to be built by the CI or developer prior to image build
COPY target/*-exec.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
