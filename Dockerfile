FROM openjdk:8-jdk-alpine as builder

# Install Maven
RUN apk add --no-cache curl tar bash
ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# Install project dependencies and keep sources
# make source folder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# install maven dependency packages (keep in image)
COPY ./cvsm-git-parser/pom.xml /usr/src/app
COPY ./cvsm-git-parser/src /usr/src/app/src
RUN /usr/bin/mvn -T 1C package

# Run Dev Phase
FROM builder
WORKDIR /app

# Copy the executable
COPY --from=builder /usr/src/app/target/cvsm-git-parser-0.0.1-SNAPSHOT.jar /app

# run application
CMD ["/usr/bin/java", "-jar", "/app/cvsm-git-parser-0.0.1-SNAPSHOT.jar"]
 
