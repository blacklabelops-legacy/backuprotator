FROM blacklabelops/jobber
MAINTAINER Steffen Bleul <sbl@blacklabelops.com>

ENV JAVA_DISTRIBUTION=server-jre \
    JAVA_MAJOR_VERSION=8 \
    JAVA_UPDATE_VERSION=92 \
    JAVA_BUILD_NUMBER=14 \
    ROTATOR_HOME=/opt/backuprotator \
    GLIBC_VERSION=2.23-r1 \
    LANG=en_US.UTF-8

ENV JAVA_VERSION=1.${JAVA_MAJOR_VERSION}.0_${JAVA_UPDATE_VERSION}
ENV JAVA_HOME=/opt/java/${JAVA_DISTRIBUTION}${JAVA_VERSION}
ENV PATH=$PATH:$JAVA_HOME/bin

RUN apk add --update \
      ca-certificates \
      unzip \
      wget && \
    # Install latest glibc
    wget --directory-prefix=/tmp https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-${GLIBC_VERSION}.apk && \
    apk add --allow-untrusted /tmp/glibc-${GLIBC_VERSION}.apk && \
    wget --directory-prefix=/tmp https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-bin-${GLIBC_VERSION}.apk && \
    apk add --allow-untrusted /tmp/glibc-bin-${GLIBC_VERSION}.apk && \
    wget --directory-prefix=/tmp https://github.com/andyshinn/alpine-pkg-glibc/releases/download/${GLIBC_VERSION}/glibc-i18n-${GLIBC_VERSION}.apk && \
    apk --allow-untrusted add /tmp/glibc-i18n-${GLIBC_VERSION}.apk && \
    /usr/glibc-compat/bin/localedef -i en_US -f UTF-8 en_US.UTF-8 && \
    # Install oracle java
    export JAVA_TARBALL=${JAVA_DISTRIBUTION}-${JAVA_MAJOR_VERSION}u${JAVA_UPDATE_VERSION}-linux-x64.tar.gz && \
    wget --directory-prefix=/tmp \
         --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" \
         http://download.oracle.com/otn-pub/java/jdk/${JAVA_MAJOR_VERSION}u${JAVA_UPDATE_VERSION}-b${JAVA_BUILD_NUMBER}/${JAVA_TARBALL} && \
    mkdir -p /opt/java && \
    tar -xzf /tmp/${JAVA_TARBALL} -C /opt/java/ && \
    if  [ "${JAVA_DISTRIBUTION}" = "server-jre" ]; \
      then mv /opt/java/jdk${JAVA_VERSION} ${JAVA_HOME} ; \
    fi && \
    ln -s ${JAVA_HOME}/bin/java /usr/bin/java && \
    # Remove obsolete packages
    apk del \
      unzip \
      wget && \
    # Clean caches and tmps
    rm -rf /var/cache/apk/* && \
    rm -rf /tmp/* && \
    rm -rf /var/log/*

COPY build/libs/backuprotator-all-0.1.0.jar /opt/backuprotator/backrotator-all.jar
COPY build/resources/main /opt/backuprotator
WORKDIR /opt/backuprotator
