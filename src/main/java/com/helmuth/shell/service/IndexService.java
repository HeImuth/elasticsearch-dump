package com.helmuth.shell.service;

import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.helmuth.shell.model.Document;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IndexService<T> {
    List<String> listIndices() throws IOException;
    String getIndexSettings(String indexName) throws IOException;
    String getIndexMapping(String indexName) throws IOException;
    void createIndex(String indexName) throws IOException;
    void deleteIndex(String indexName) throws IOException;
    long countDocuments(String indexName) throws IOException;
    void indexDocument(T document);
    void indexDocuments(String indexName, Collection<T> documents) throws IOException;
    void indexDocument(String indexName, Map<String, Object> document);
    Optional<Document> getDocumentById(String indexName, String id) throws IOException;
    List<Document> getDocuments(String indexName, int size, int page) throws IOException;
    SearchResponse<Document> scrollSearch(String indexName, int size, String timeout, List<String> includeFields, List<String> excludeFields) throws IOException;
    ScrollResponse<Document> scroll(String scrollId, String timeout) throws IOException;
    void clearScroll(String scrollId) throws IOException;
}
