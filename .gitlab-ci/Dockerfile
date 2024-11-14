FROM maven:3.9-eclipse-temurin-17-focal

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt-get -qq update && apt-get -y dist-upgrade && apt-get install -qq -y mariadb-client xmlstarlet && apt-get clean && rm -rf /var/lib/apt/lists/*
