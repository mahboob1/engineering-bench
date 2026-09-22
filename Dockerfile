FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew bootJar -x test


FROM eclipse-temurin:21-jre

WORKDIR /app

# Install tools required by Engineering Bench to invoke
# AWS ECS Exec / Systems Manager Session Manager.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       curl \
       ca-certificates \
       unzip \
    && curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/ubuntu_arm64/session-manager-plugin.deb" \
       -o /tmp/session-manager-plugin.deb \
    && dpkg -i /tmp/session-manager-plugin.deb \
    && rm -f /tmp/session-manager-plugin.deb \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

CMD ["java","-jar","app.jar"]