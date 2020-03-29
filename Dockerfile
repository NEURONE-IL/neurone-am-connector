#
# Scala and sbt Dockerfile
#
# https://github.com/hseeberger/scala-sbt
#

# Pull base image
FROM openjdk:8u171-alpine3.8 AS build

# Env variables
ENV SCALA_VERSION 2.12.7
ENV SBT_VERSION 1.2.6


ENV PATH /sbt/bin:$PATH

RUN apk add -U bash docker

# Install Scala
## Piping curl directly in tar
RUN \
  wget -O - https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install SBT
RUN wget https://piccolo.link/sbt-$SBT_VERSION.tgz && \
  tar -xzvf sbt-$SBT_VERSION.tgz && \
  sbt sbtVersion
# Define working directory


WORKDIR /neurone-conector
COPY build.sbt .
COPY project project
COPY src src
COPY .env .env
RUN sbt stage


##########3

FROM openjdk:8-jre-alpine


COPY --from=build /neurone-conector/target/universal/stage stage


WORKDIR /stage/bin/
COPY .env .env
RUN ls
RUN apk add --no-cache --upgrade bash

CMD ["./neurone-conector" ] 

