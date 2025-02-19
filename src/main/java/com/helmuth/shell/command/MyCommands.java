package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.model.SampleDocument;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@ShellComponent()
public class MyCommands {
    private final ElasticsearchClient client;

    public MyCommands(ElasticsearchClient elasticsearchClient) {
        this.client = elasticsearchClient;
    }


    @ShellMethod(key = "hello-world", value = "Prints 'Hello, World' message")
    public String helloWorld(@ShellOption(defaultValue = "spring") String arg) {
        return "Hello, World " + arg;
    }

    @ShellMethod(key = "now", value = "Prints current time", group = "Time")
    public String now(@ShellOption(defaultValue = "HH:mm:ss") String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    @ShellMethod(key = "search-all", value = "Search all documents in the index")
    public List<SampleDocument> searchAll(@ShellOption(defaultValue = "") String index, @ShellOption(defaultValue = "") String id) throws IOException {
        if(index.isEmpty()) {
            System.err.println("Index parameter is required");
            return null;
        }

        if(id != null && !id.isEmpty()) {
            GetResponse<SampleDocument> response = client.get(request -> request.index(index).id(id), SampleDocument.class);
            return Collections.singletonList(response.source());
        }

        SearchResponse<SampleDocument> response = client.search(request -> request.index(index), SampleDocument.class);
        return response.hits().hits().stream().map(Hit::source).toList();
    }
}
