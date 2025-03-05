package com.helmuth.shell.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.List;
import java.util.Map;

public class DocumentExporter {
    private static final String CSV_EXTENSION = ".csv";
    private static final String JSON_EXTENSION = ".json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CSVWriter csvWriter;
    private PrintWriter jsonWriter;
    private String fileExtension = CSV_EXTENSION;
    private final String output;
    private final boolean outputHeader;

    public DocumentExporter(String output, boolean outputHeader) {
        this.output = output;
        this.outputHeader = outputHeader;
    }

    public void initialize() throws IOException {
        if (output != null && !output.isEmpty()) {
            fileExtension = getFileExtension(output);
            if (fileExtension.equals(JSON_EXTENSION)) {
                jsonWriter = new PrintWriter(new FileWriter(output));
                jsonWriter.print("["); // Start JSON array
            } else {
                csvWriter = new CSVWriter(new FileWriter(output));
            }
        }
    }

    public void close() throws IOException {
        if (csvWriter != null) {
            csvWriter.close();
        }
        if (jsonWriter != null) {
            jsonWriter.print("]");
            jsonWriter.close();
        }
    }

    public void writeDocuments(List<Document> documents, boolean firstBatch) {
        if (!firstBatch && documents.isEmpty()) {
            return;
        }

        if (outputHeader && fileExtension.equals(CSV_EXTENSION) && firstBatch && !documents.isEmpty()) {
            printHeader(documents.getFirst());
        }
        
        if (csvWriter != null) {
            writeDocumentsIntoCsvFile(documents);
        } else if (jsonWriter != null) {
            writeDocumentsIntoJsonFile(documents, !firstBatch);
        } else {
            printInConsole(documents);
        }
    }

    private static String getFileExtension(String output) {
        if (output == null || !output.contains(".")) {
            return CSV_EXTENSION;
        }
        return output.substring(output.lastIndexOf(".")).toLowerCase();
    }

    private void printHeader(Document document) {
        if (document == null) {
            return;
        }
        if (csvWriter != null) {
            String[] headers = document.keySet().toArray(new String[0]);
            csvWriter.writeNext(headers);
        } else {
            System.out.println(document.keySet());
        }
    }

    private void writeDocumentsIntoJsonFile(List<Document> documents, boolean anyProcessedYet) {
        if (jsonWriter != null) {
            for (int i = 0; i < documents.size(); i++) {
                try {
                    if (anyProcessedYet && i == 0) {
                        jsonWriter.println(",");
                    }
                    String json = objectMapper.writeValueAsString(documents.get(i));
                    if (i == (documents.size() - 1)) {
                        jsonWriter.print(json);
                    } else {
                        jsonWriter.println(json + ",");
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeDocumentsIntoCsvFile(List<Document> documents) {
        documents.forEach(doc -> {
            if (doc != null && !doc.isEmpty()) {
                String[] data = doc.values().stream()
                        .map(this::printField)
                        .toArray(String[]::new);
                csvWriter.writeNext(data);
            }
        });
    }

    private static void printInConsole(List<Document> documents) {
        documents.forEach(System.out::println);
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
