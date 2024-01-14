FROM tomcat:9.0-jdk17-temurin-jammy

ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mv /usr/local/tomcat/webapps.dist/manager /usr/local/tomcat/webapps/ && mkdir /srv/submissioninterface

COPY context.xml /usr/local/tomcat/webapps/manager/META-INF/

COPY tomcat-users.xml /usr/local/tomcat/conf/

COPY *.jar /srv/submissioninterface/
COPY plaggie.properties /srv/submissioninterface/
COPY lectures /srv/submissioninterface/

RUN adduser --uid 9987 --home /usr/local/tomcat --no-create-home --shell /sbin/nologin --disabled-password --system tomcat && mkdir /usr/local/tomcat/work/sessions/ && chown tomcat /usr/local/tomcat/work/sessions/ && mkdir -p /usr/local/tomcat/conf/Catalina/localhost && chown tomcat /usr/local/tomcat/conf/Catalina/localhost && chown tomcat /usr/local/tomcat/webapps/ && chown -R tomcat /srv/submissioninterface
USER tomcat
