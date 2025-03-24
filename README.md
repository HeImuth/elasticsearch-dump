# Elasticsearch Dump Shell

A command-line tool for exporting and manipulating Elasticsearch data. This tool provides an interactive shell interface to work with Elasticsearch indices and documents.

## Features

- Export documents from Elasticsearch indices
- Query and manipulate index data
- Interactive shell interface for easy operation
- Support for authenticated Elasticsearch connections

## Prerequisites

- Java 21 or later
- Maven (for building from source)
- GraalVM (for native image compilation)
- Docker (optional, for running Elasticsearch locally)

## Environment Variables

Before running the application, set the following environment variables:

- `ELASTICSEARCH_HOST`: The URL of your Elasticsearch instance (e.g., `http://localhost:9200`)
- `ELASTICSEARCH_USERNAME`: Username for authentication (default: elastic)
- `ELASTICSEARCH_PASSWORD`: Password for authentication

## Running Elasticsearch Locally

A docker-compose.yml file is provided to run Elasticsearch locally with basic security enabled:

```yaml
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.2
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
      - ELASTIC_PASSWORD=password  # Default password for 'elastic' user
      - xpack.security.enabled=true
      - xpack.security.enrollment.enabled=true
    volumes:
      - ./elasticsearch_data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
```

To start Elasticsearch:
```bash
docker-compose up -d
```

## Running the Application

### Using Java JAR

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   export ELASTICSEARCH_HOST=http://localhost:9200
   export ELASTICSEARCH_USERNAME=elastic
   export ELASTICSEARCH_PASSWORD=password
   java -jar target/elasticsearch-dump-shell-0.1.0.jar
   ```

### Using Native Image

1. Make sure GraalVM is installed and configured.

2. Build the native image:
   ```bash
   mvn -Pnative native:compile
   ```

3. Run the native application:
   ```bash
   export ELASTICSEARCH_HOST=http://localhost:9200
   export ELASTICSEARCH_USERNAME=elastic
   export ELASTICSEARCH_PASSWORD=password
   ./target/elasticsearch-dump-shell
   ```

## Usage

Once the application is running, you'll see the prompt:
```
shell>
```

Available commands will be shown by typing `help`.
