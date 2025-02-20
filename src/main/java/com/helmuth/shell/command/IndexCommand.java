package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.model.GenericDocument;
import com.helmuth.shell.service.IndexService;
import com.opencsv.CSVWriter;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Command(group = "index", description = "Index operations")
public class IndexCommand {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IndexService<Document> indexService;

    public IndexCommand(IndexService<Document> indexService) {
        this.indexService = indexService;
    }

    @Command(command = "list", description = "List all indexes")
    public void listIndexes() {
        try {
            indexService.listIndices().forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Failed to list indices");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "settings", description = "Get index settings")
    public void getIndexSettings(String indexName) {
        try {
            String settings = indexService.getIndexSettings(indexName);
            System.out.println(settings);
        }catch (Exception e) {
            System.err.println("Failed to get index settings");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "mappings", description = "Get index settings")
    public void getIndexMappings(String indexName) {
        try {
            String mappings = indexService.getIndexMapping(indexName);
            System.out.println(mappings);
        }catch (Exception e) {
            System.err.println("Failed to get index settings");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "create", description = "Create an index")
    public void createIndex(String indexName) {
        try {
            indexService.createIndex(indexName);
            System.out.println("Index created: " + indexName);
        } catch (Exception e) {
            System.err.println("Failed to create index");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "delete", description = "Delete an index", group = "index")
    public void deleteIndex(String indexName) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to delete the index " + indexName + "? (yes/no)");
        String response = scanner.nextLine();

        if (!response.equalsIgnoreCase("yes")) {
            System.out.println("Index deletion cancelled");
            return;
        }

        try {
            indexService.deleteIndex(indexName);
            System.out.println("Index deleted: " + indexName);
        } catch (Exception e) {
            System.err.println("Failed to delete index");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "count", description = "Count the number of documents in an index", group = "index")
    public void countDocuments(String indexName) {
        try {
            long count = indexService.countDocuments(indexName);
            System.out.println("Number of documents in index " + indexName + ": " + count);
        } catch (Exception e) {
            System.err.println("Failed to count documents");
            throw new RuntimeException(e);
        }
    }


    @Command(command = "scroll", description = "Scroll through documents in an index", group = "index")
    public void scroll(String indexName, @Option(defaultValue = "100") int size, @Option(defaultValue = "10m") String timeout,
                       @Option List<String> includeFields, @Option List<String> excludeFields,
                       @Option String output, @Option(defaultValue = "false") Boolean outputHeader) throws IOException {
        CSVWriter writer = null;
        ScrollResponse<Document> scroll = null;
        String outputFormat = ".csv";
        try {
            if (output != null && !output.isEmpty()) {
                writer = new CSVWriter(new FileWriter(output));
                outputFormat = output.substring(output.lastIndexOf("."));
            }
            SearchResponse<Document> initScroll = indexService.scrollSearch(indexName, size, timeout, includeFields, excludeFields);
            List<Hit<Document>> searchHits = initScroll.hits().hits();
            if (outputHeader) {
                printHeader(writer, searchHits);
            }
            processHits(writer, searchHits);

            scroll = indexService.scroll(initScroll.scrollId(), timeout);
            List<Hit<Document>> hits = scroll.hits().hits();
            while (!hits.isEmpty()) {
                processHits(writer, hits);
                scroll = indexService.scroll(scroll.scrollId(), timeout);
                hits = scroll.hits().hits();
            }

        } catch (Exception e) {
            System.err.println("Failed to scroll through documents");
            throw new RuntimeException(e);
        } finally {
            if (scroll != null && scroll.scrollId() != null) {
                indexService.clearScroll(scroll.scrollId());
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void printHeader(CSVWriter writer, List<Hit<Document>> searchHits) {
        if (searchHits.isEmpty() || searchHits.getFirst() == null || searchHits.getFirst().source() == null) {
            return;
        }
        if (writer != null) {
            String[] headers = searchHits.getFirst().source().keySet().toArray(new String[0]);
            writer.writeNext(headers);
        } else {
            System.out.println(searchHits.getFirst().source().keySet());
        }
    }

    private void processHits(CSVWriter writer, List<Hit<Document>> hits) {
        if (writer != null) {
            writeDocuments(hits, writer);
        } else {
            printInConsole(hits);
        }
    }

    private static void printInConsole(List<Hit<Document>> hits) {
        hits.forEach(hit -> System.out.println(hit.source()));
    }

    private void writeDocuments(List<Hit<Document>> hits, CSVWriter writer) {
        hits.forEach(hit -> {
            Map<String, Object> source = hit.source();
            if (source != null && !source.isEmpty()) {
                String[] data = source.values().stream().map(this::printField).toArray(String[]::new);
                writer.writeNext(data);
            }
        });
    }

    private String printField(Object field) {
        if (field == null) {
            return "";
        }

        if (field instanceof String) {
            return field.toString();
        } else {
            try {
                return objectMapper.writeValueAsString(field);
            } catch (IOException e) {
                return String.valueOf(field);
            }
        }
    }


}
