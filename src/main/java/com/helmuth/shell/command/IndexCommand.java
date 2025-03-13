package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.service.IndexService;
import com.helmuth.shell.util.DocumentExporter;
import com.helmuth.shell.util.UserConfirmationUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
        } catch (Exception e) {
            System.err.println("Failed to get index settings");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "mappings", description = "Get index settings")
    public void getIndexMappings(String indexName) {
        try {
            String mappings = indexService.getIndexMapping(indexName);
            System.out.println(mappings);
        } catch (Exception e) {
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
        try {
            if (!UserConfirmationUtil.confirmAction("delete the index " + indexName)) {
                System.out.println("Index deletion cancelled");
                return;
            }
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
        DocumentExporter exporter = new DocumentExporter(output, outputHeader);
        ScrollResponse<Document> scroll = null;
        try {
            exporter.initialize();
            SearchResponse<Document> initScroll = indexService.scrollSearch(indexName, size, timeout, includeFields, excludeFields);
            List<Document> documents = initScroll.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
            exporter.writeDocuments(documents, true);

            scroll = indexService.scroll(initScroll.scrollId(), timeout);
            List<Document> hits = scroll.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
            while (!hits.isEmpty()) {
                exporter.writeDocuments(hits, false);
                scroll = indexService.scroll(scroll.scrollId(), timeout);
                hits = scroll.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
            }
        } catch (Exception e) {
            System.err.println("Failed to scroll through documents");
            throw new RuntimeException(e);
        } finally {
            if (scroll != null && scroll.scrollId() != null) {
                indexService.clearScroll(scroll.scrollId());
            }
            try {
                exporter.close();
            } catch (IOException e) {
                System.err.println("Failed to close export writers");
                e.printStackTrace();
            }
        }
    }

    @Command(command = "import", description = "Import documents into an index")
    public void importDocuments(String indexName, @Option(required = true) String file) {
        try {
            String fileFormat = file.substring(file.lastIndexOf("."));
            if (!fileFormat.equals(".json") && !fileFormat.equals(".csv")) {
                System.err.println("Only JSON and CSV files are supported at the moment");
                return;
            }

            File fileToImport = new File(file);

            List<Document> documents = switch (fileFormat) {
                case ".json" -> documents = readJson(fileToImport);
                case ".csv" -> documents = readCsv(fileToImport);
                default -> new ArrayList<>();
            };

            if (documents.isEmpty()) {
                System.out.println("No documents to import");
                return;
            }

            indexService.indexDocuments(indexName, documents);
            System.out.println("Documents imported successfully");
        } catch (Exception e) {
            System.err.println("Failed to import documents");
            throw new RuntimeException(e);
        }
    }

    private static List<Document> readJson(File fileToImport) throws IOException {
        try {
            return objectMapper.readValue(fileToImport, new TypeReference<List<Document>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private List<Document> readCsv(File fileToImport) {
        System.out.println("The first row will be used as the header row");
        if (!UserConfirmationUtil.confirm("Do you want to continue?")) {
            System.out.println("Import cancelled");
            return Collections.emptyList();
        }

        List<Document> documents = new ArrayList<>();
        RFC4180Parser parser = new RFC4180Parser();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(fileToImport))
                .withCSVParser(parser)
                .build()) {

            String[] headers = reader.readNext();

            String[] row;
            while ((row = reader.readNext()) != null) {
                HashMap<String, Object> fields = new HashMap<>();

                for (int i = 0; i < row.length && i < headers.length; i++) {
                    String value = row[i].trim();

                    // Handle JSON objects
                    if (value.startsWith("{")) {
                        fields.put(headers[i], objectMapper.readValue(value, Map.class));
                    }
                    // Handle JSON arrays
                    else if (value.startsWith("[")) {
                        fields.put(headers[i], objectMapper.readValue(value, List.class));
                    }
                    // Handle regular strings
                    else {
                        fields.put(headers[i], value);
                    }
                }

                documents.add(new Document(null, fields));
            }
        } catch (CsvValidationException | IOException e) {
            return Collections.emptyList();
        }

        return documents;
    }
}
