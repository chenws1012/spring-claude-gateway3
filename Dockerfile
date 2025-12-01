FROM amazoncorretto:17-alpine
LABEL maintainer="chenwenshun@gmail.com"

ENV JAR="gateway.jar"
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo 'Asia/Shanghai' > /etc/timezone

RUN mkdir /app
COPY ./target/$JAR /app
WORKDIR /app

EXPOSE 9000

# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD curl -f http://localhost:9000/actuator/health || exit 1

ENTRYPOINT java $JAVA_OPTS -XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -Dreactor.netty.http.server.accessLogEnabled=true -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar $JAR

