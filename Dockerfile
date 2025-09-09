FROM swr.cn-east-3.myhuaweicloud.com/woody-public/ubuntu-22.4:1.8.441_skywalking-9.4
#FROM openjdk:8-jre-alpine
MAINTAINER from shwoody.com by wenshun.chen (chenwenshun@shwoody.com)

ENV JAR="gateway.jar"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

RUN mkdir /app
COPY ./target/$JAR /app
ENV TZ=Asia/Shanghai
WORKDIR /app

EXPOSE 9000

ENTRYPOINT java $JAVA_OPTS -jar -XX:+UseContainerSupport -XX:InitialRAMPercentage=50.0 -XX:MaxRAMPercentage=75.0 -Dreactor.netty.http.server.accessLogEnabled=true -Dspring.profiles.active=$SPRING_PROFILE $JAR

