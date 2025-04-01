FROM docker.elastic.co/elasticsearch/elasticsearch:8.8.2 AS elasticsearch

# Copy Elasticsearch configuration and data
FROM debian:bookworm-slim

# Install Elasticsearch dependencies
RUN apt-get update && apt-get install -y \
    bash \
    openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

# Copy Elasticsearch from the official image
COPY --from=elasticsearch /usr/share/elasticsearch /usr/share/elasticsearch
COPY --from=elasticsearch /usr/local/bin/docker-entrypoint.sh /usr/local/bin/

# Set up Elasticsearch environment
ENV ELASTIC_CONTAINER=true
ENV PATH=/usr/share/elasticsearch/bin:$PATH
ENV ES_JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ES_HOME=/usr/share/elasticsearch
ENV ELASTIC_PASSWORD=password
ENV discovery.type=single-node
ENV xpack.security.enabled=true
ENV xpack.security.enrollment.enabled=true
ENV ES_JAVA_OPTS="-Xms512m -Xmx512m"

# Create elasticsearch user and group
RUN groupadd -g 1000 elasticsearch && \
    useradd -u 1000 -g elasticsearch elasticsearch

# Set correct permissions
RUN mkdir -p /usr/share/elasticsearch/data && \
    chown -R elasticsearch:elasticsearch /usr/share/elasticsearch

# Copy the native application
WORKDIR /app
COPY target/elasticsearch-dump-shell .
RUN chmod +x elasticsearch-dump-shell

# Create startup script
RUN echo '#!/bin/bash\n\
/usr/local/bin/docker-entrypoint.sh eswrapper & \
sleep 30 && \
ELASTICSEARCH_HOST=localhost:9200 \
ELASTICSEARCH_USERNAME=elastic \
ELASTICSEARCH_PASSWORD=password \
./elasticsearch-dump-shell\n' > /start.sh && \
chmod +x /start.sh

EXPOSE 9200 9300

ENTRYPOINT ["/start.sh"]
