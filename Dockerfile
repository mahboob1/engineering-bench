FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew bootJar -x test


FROM eclipse-temurin:21-jre

WORKDIR /app

# Install tools required by Engineering Bench to invoke
# AWS ECS Exec / Systems Manager Session Manager.
ARG TARGETARCH

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       curl \
       ca-certificates \
       unzip \
    && if [ "$TARGETARCH" = "arm64" ]; then \
         SESSION_MANAGER_ARCH="ubuntu_arm64"; \
       elif [ "$TARGETARCH" = "amd64" ]; then \
         SESSION_MANAGER_ARCH="ubuntu_64bit"; \
       else \
         echo "Unsupported architecture: $TARGETARCH" && exit 1; \
       fi \
    && curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/${SESSION_MANAGER_ARCH}/session-manager-plugin.deb" \
       -o /tmp/session-manager-plugin.deb \
    && dpkg -i /tmp/session-manager-plugin.deb \
    && rm -f /tmp/session-manager-plugin.deb \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*.jar app.jar

CMD ["java","-jar","app.jar"]