# Elasticsearch Dump Shell

A command-line tool for exporting and manipulating Elasticsearch data. This tool provides an interactive shell interface to work with Elasticsearch indices and documents.

## Features

- Export documents from Elasticsearch indices
- Query and manipulate index data
- Interactive shell interface for easy operation

## Prerequisites

- Java 21 or later
- Maven (for building from source)
- GraalVM (for native image compilation)

## Environment Variables

Before running the application, set the following environment variable:

- `ELASTICSEARCH_HOST`: The URL of your Elasticsearch instance (e.g., `http://localhost:9200`)

## Running the Application

### Using Java JAR

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   export ELASTICSEARCH_HOST=http://localhost:9200
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
   ./target/elasticsearch-dump-shell
   ```

## Usage

Once the application is running, you'll see the prompt:
```
shell>
```

Available commands will be shown by typing `help`.
