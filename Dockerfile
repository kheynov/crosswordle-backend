FROM openjdk:11
ARG PORT
EXPOSE $PORT:$PORT
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]