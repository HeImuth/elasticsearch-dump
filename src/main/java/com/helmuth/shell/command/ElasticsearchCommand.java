package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cat.IndicesResponse;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.helmuth.shell.model.SampleDocument;
import org.springframework.shell.command.annotation.Command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Command(command = "index", group = "index")
public class ElasticsearchCommand {

    private final ElasticsearchClient client;

    public ElasticsearchCommand(ElasticsearchClient client) {
        this.client = client;
    }


    @Command(command = "list", description = "List all indexes", group = "index")
    public List<String> listIndexes() throws IOException {
        IndicesResponse indices = client.cat().indices();
        return indices.valueBody().stream().map(IndicesRecord::index).toList();
    }

    @Command(command = "mappings", description = "List all indexes", group = "index")
    public String mappings(String index) throws IOException {
        GetMappingResponse response = client.indices().getMapping(request -> request.index(index));
        String mappings = Objects.requireNonNull(response.get(index)).mappings().toString();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object json = objectMapper.readValue(mappings.replaceFirst("^TypeMapping: ", ""), Object.class);
        return objectMapper.writeValueAsString(json);
    }


    @Command(command = "create sample", description = "Create a sample index", group = "index")
    public void createSampleIndex() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SampleDocument> sampleDocuments = objectMapper.readValue(
                new File("src/main/resources/sample.json"), new TypeReference<List<SampleDocument>>() {});


        String indexName = "sample_index";
        client.indices().create(c -> c.index(indexName));

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        for (SampleDocument document : sampleDocuments) {
            bulkRequest.operations(op -> op.index(i -> i.index(indexName).document(document)));
        }

        client.bulk(bulkRequest.build());
        System.out.println("Index created and documents inserted successfully");
    }


}
