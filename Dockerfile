FROM amazoncorretto:17

LABEL maintainer="chenwenshun@gmail.com"

ARG JAR=gateway.jar
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""

RUN yum install -y tzdata \
 && ln -sf /usr/share/zoneinfo/$TZ /etc/localtime \
 && yum clean all

WORKDIR /app
COPY target/${JAR} app.jar

EXPOSE 9000

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dreactor.netty.http.server.accessLogEnabled=true", \
  "-jar", "/app/app.jar"]

CMD ["--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]