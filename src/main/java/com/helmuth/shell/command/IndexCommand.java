package com.helmuth.shell.command;

import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.model.GenericDocument;
import com.helmuth.shell.service.IndexService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180Parser;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.*;
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
        CSVWriter csvWriter = null;
        PrintWriter jsonWriter = null;
        ScrollResponse<Document> scroll = null;
        String outputFormat = ".csv";
        try {
            if (output != null && !output.isEmpty()) {
                String fileExtension = output.substring(output.lastIndexOf(".")).toLowerCase();
                if (fileExtension.equals(".json")) {
                    outputFormat = "json";
                    jsonWriter = new PrintWriter(new FileWriter(output));
                    jsonWriter.println("["); // Start JSON array
                } else {
                    outputFormat = "csv";
                    csvWriter = new CSVWriter(new FileWriter(output));
                }
            }
            SearchResponse<Document> initScroll = indexService.scrollSearch(indexName, size, timeout, includeFields, excludeFields);
            List<Hit<Document>> searchHits = initScroll.hits().hits();
            if (outputHeader && outputFormat.equals("csv")) {
                printHeader(csvWriter, searchHits);
            }
            processHits(csvWriter, jsonWriter, searchHits);

            scroll = indexService.scroll(initScroll.scrollId(), timeout);
            List<Hit<Document>> hits = scroll.hits().hits();
            while (!hits.isEmpty()) {
                processHits(csvWriter, jsonWriter, hits);
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
            if (csvWriter != null) {
                csvWriter.close();
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
        System.out.print("Do you want to continue? [y/n]: ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
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

    private void processHits(CSVWriter writer, PrintWriter jsonWriter, List<Hit<Document>> hits) {
        if(writer != null) {
            writeHitsIntoCsvFile(writer, hits);
        } else if(jsonWriter != null) {
            writeHitsIntoJsonFile(jsonWriter, hits);
        } else{
            printInConsole(hits);
        }
    }

    private void writeHitsIntoJsonFile(PrintWriter jsonWriter, List<Hit<Document>> hits) {
        if (jsonWriter != null) {
            hits.forEach(hit -> {
                try {
                    jsonWriter.println(objectMapper.writeValueAsString(hit.source()) + ",");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void writeHitsIntoCsvFile(CSVWriter writer, List<Hit<Document>> hits) {
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
