FROM openjdk:8
MAINTAINER Nikhil Vasaikar <nikhilvasaikar.@gmail.com>
ADD target/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar
ENTRYPOINT ["java", "-jar", "/eureka-server.jar"]
EXPOSE 8761