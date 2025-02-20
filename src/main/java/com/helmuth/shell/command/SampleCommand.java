package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.service.IndexService;
import org.springframework.shell.command.annotation.Command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Command(group = "sample", description = "Sample commands")
public class SampleCommand {
    private final ElasticsearchClient client;
    private final IndexService<Document> indexService;

    public SampleCommand(ElasticsearchClient client, IndexService<Document> indexService) {
        this.client = client;
        this.indexService = indexService;
    }

    @Command(command = "create-sample", description = "Create a sample index")
    public void createSampleIndex() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Document> sampleDocuments = objectMapper.readValue(
                new File("src/main/resources/sample.json"), new TypeReference<List<Document>>() {
                });

        String indexName = "sample_index";
        try {
            Optional<String> sampleIndex = indexService.listIndices().stream()
                    .filter(index -> index.equals(indexName))
                    .findFirst();
            if (sampleIndex.isPresent()) {
                boolean deleted = deleteSampleIndex(indexName);
                if (!deleted) {
                    return;
                }
            }
            client.indices().create(c -> c.index(indexName));
        } catch (Exception e) {
            System.err.println("Failed to create index");
            return;
        }

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        for (Document document : sampleDocuments) {
            bulkRequest.operations(op -> op.index(i -> i.index(indexName).document(document)));
        }

        client.bulk(bulkRequest.build());
        System.out.println("Index created and documents inserted successfully");
    }

    private boolean deleteSampleIndex(String indexName) {
        System.out.println("Index already exists. You want to delete it first? [y/n]");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
            try {
                indexService.deleteIndex(indexName);
            } catch (IOException e) {
                System.err.println("Failed to delete index");
                return false;
            }
        }
        return true;
    }

}
