FROM eclipse-temurin:21-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       git \
       bash \
       curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/bash sandbox

WORKDIR /workspace

RUN chown -R sandbox:sandbox /workspace

USER sandbox

CMD ["bash"]