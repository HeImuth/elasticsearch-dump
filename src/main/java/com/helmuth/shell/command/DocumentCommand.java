package com.helmuth.shell.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.service.IndexService;
import com.helmuth.shell.util.DocumentExporter;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Command(group = "document", description = "Document operations")
public class DocumentCommand {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IndexService<Document> indexService;

    public DocumentCommand(IndexService<Document> indexService) {
        this.indexService = indexService;
    }

    @Command(command = "index-document", description = "Index a document")
    public void indexDocument(@Option() String indexName, @Option() String document) {
        try {
            HashMap<String, Object> documentMap = objectMapper.readValue(document, new TypeReference<HashMap<String, Object>>() {});
            indexService.indexDocument(indexName, new Document(null, documentMap));
            System.out.println("Document indexed successfully");
        } catch (Exception e) {
            System.err.println("Failed to index document");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "get-document", description = "Get a document by ID")
    public String getDocument(@Option String indexName, @Option String id) {
        try {
            Document document = indexService.getDocumentById(indexName, id).orElse(null);
            if (document == null) {
                return "Document not found";
            }
            return document.toString();
        } catch (Exception e) {
            System.err.println("Failed to get document");
            throw new RuntimeException(e);
        }
    }

    @Command(command = "list-documents", description = "List documents in an index by page")
    public void listDocuments(String indexName, 
                            @Option(defaultValue = "20") int size, 
                            @Option(defaultValue = "0") int page,
                            @Option String output, 
                            @Option(defaultValue = "false") Boolean outputHeader,
                            @Option List<String> includeFields,
                            @Option List<String> excludeFields) {
        DocumentExporter exporter = new DocumentExporter(output, outputHeader);
        try {
            exporter.initialize();
            List<Document> documents = indexService.getDocuments(indexName, size, page, includeFields, excludeFields);
            exporter.writeDocuments(documents, true);
        } catch (Exception e) {
            System.err.println("Failed to list documents");
            throw new RuntimeException(e);
        } finally {
            try {
                exporter.close();
            } catch (IOException e) {
                System.err.println("Failed to close export writers");
                e.printStackTrace();
            }
        }
    }

    @Command(command = "search-documents", description = "search documents in an index by page")
    public void searchDocuments(String indexName, 
                              @Option(defaultValue = "") String query, 
                              @Option(defaultValue = "20") int size, 
                              @Option(defaultValue = "0") int page, 
                              @Option String output, 
                              @Option(defaultValue = "false") Boolean outputHeader,
                              @Option List<String> includeFields,
                              @Option List<String> excludeFields) {
        DocumentExporter exporter = new DocumentExporter(output, outputHeader);
        try {
            exporter.initialize();
            List<Document> documents = indexService.searchDocuments(indexName, query, size, page, includeFields, excludeFields);
            exporter.writeDocuments(documents, true);
        } catch (Exception e) {
            System.err.println("Failed to search documents");
            throw new RuntimeException(e);
        } finally {
            try {
                exporter.close();
            } catch (IOException e) {
                System.err.println("Failed to close export writers");
                e.printStackTrace();
            }
        }
    }

}
