FROM blacklabelops/jobber
MAINTAINER Steffen Bleul <sbl@blacklabelops.com>

ENV JAVA_DISTRIBUTION=jre \
    JAVA_MAJOR_VERSION=8 \
    JAVA_UPDATE_VERSION=latest \
    JAVA_BUILD_NUMBER= \
    ROTATOR_HOME=/opt/backuprotator

RUN if  [ "${JAVA_DISTRIBUTION}" = "jre" ]; \
      then export JAVA_PACKAGE_POSTFIX_VERSION=-jre ; \
      else export JAVA_PACKAGE_POSTFIX_VERSION= ; \
    fi && \
    export JAVA_VERSION=${JAVA_MAJOR_VERSION}.${JAVA_UPDATE_VERSION}.${JAVA_BUILD_NUMBER} && \
    if  [ "${JAVA_UPDATE_VERSION}" = "latest" ]; \
      then apk add --update openjdk${JAVA_MAJOR_VERSION}${JAVA_PACKAGE_POSTFIX_VERSION} ; \
      else apk add --update "openjdk${JAVA_MAJOR_VERSION}${JAVA_PACKAGE_POSTFIX_VERSION}=${JAVA_VERSION}" ; \
    fi && \
    # Clean caches and tmps
    rm -rf /var/cache/apk/* && \
    rm -rf /tmp/* && \
    rm -rf /var/log/*

ENV JAVA_HOME=/usr/lib/jvm/default-jvm
ENV PATH=$JAVA_HOME/bin:$PATH

COPY build/libs/backuprotator-all-0.1.0.jar /opt/backuprotator/backrotator-all.jar
COPY build/resources/main /opt/backuprotator
WORKDIR /opt/backuprotator
