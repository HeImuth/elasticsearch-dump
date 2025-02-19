package com.helmuth.shell.elasticsearch.checker;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchHealthChecker {
    private final ElasticsearchClient client;

    public ElasticsearchHealthChecker(ElasticsearchClient client) {
        this.client = client;
    }

    public void checkConnectivity() {
        try {
            if (!client.ping().value()) {
                throw new RuntimeException("Failed to connect to Elasticsearch cluster");
            }
            System.out.println("Connected to Elasticsearch cluster " + client.info().clusterName());
        } catch (IOException | ElasticsearchException e) {
            System.err.println("Failed to connect to Elasticsearch cluster");
            throw new RuntimeException(e);
        }
    }

    public void checkClusterHealth() {
        try {
            var health = client.cluster().health();
            System.out.println("Elasticsearch cluster health: " + health.status());
        } catch (IOException | ElasticsearchException e) {
            System.err.println("Failed to retrieve Elasticsearch cluster health");
            throw new RuntimeException(e);
        }
    }
}
