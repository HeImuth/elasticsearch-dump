package com.helmuth.shell.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.cat.indices.IndicesRecord;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import co.elastic.clients.elasticsearch.indices.GetIndicesSettingsResponse;
import co.elastic.clients.elasticsearch.indices.GetMappingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.helmuth.shell.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class IndexServiceImpl implements IndexService<Document> {
    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);
    private static final Pattern AND_PATTERN = Pattern.compile("\\band\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOT_PATTERN = Pattern.compile("\\bnot\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern OR_PATTERN = Pattern.compile("\\bor\\b", Pattern.CASE_INSENSITIVE);
    private final ElasticsearchClient client;

    public IndexServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public List<String> listIndices() throws IOException {
        return client.cat().indices().valueBody().stream().map(IndicesRecord::index).toList();
    }

    @Override
    public String getIndexSettings(String indexName) throws IOException {
        GetIndicesSettingsResponse response = client.indices().getSettings(request -> request.index(indexName));
        String mappings = Objects.requireNonNull(response.get(indexName)).settings().toString();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object json = objectMapper.readValue(mappings.replaceFirst("^IndexSettings: ", ""), Object.class);
        return objectMapper.writeValueAsString(json);
    }

    @Override
    public String getIndexMapping(String indexName) throws IOException {
        GetMappingResponse response = client.indices().getMapping(request -> request.index(indexName));
        String mappings = Objects.requireNonNull(response.get(indexName)).mappings().toString();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object json = objectMapper.readValue(mappings.replaceFirst("^TypeMapping: ", ""), Object.class);
        return objectMapper.writeValueAsString(json);
    }

    @Override
    public void createIndex(String indexName) throws IOException {
        client.indices().create(req -> req.index(indexName));
    }

    @Override
    public void deleteIndex(String indexName) throws IOException {
        client.indices().delete(req -> req.index(indexName));
    }

    @Override
    public long countDocuments(String indexName) throws IOException {
        return client.count(req -> req.index(indexName)).count();
    }


    @Override
    public void indexDocuments(String indexName, Collection<Document> documents) throws IOException {
        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        for (Document document : documents) {
            bulkRequest.operations(op -> op.index(i -> i.index(indexName).document(document)));
        }
        client.bulk(bulkRequest.build());
    }


    @Override
    public Optional<Document> getDocumentById(String indexName, String id) throws IOException {
        GetResponse<Document> response = client.get(req -> req.index(indexName).id(id), Document.class);
        if (response.found()) {
            return Optional.of(new Document(response.id(), response.source()));
        }
        return Optional.empty();
    }

    @Override
    public List<Document> getDocuments(String indexName, int size, int page) throws IOException {
        return getDocuments(indexName, size, page, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public List<Document> getDocuments(String indexName, int size, int page, List<String> includeFields, List<String> excludeFields) throws IOException {
        SearchResponse<Document> search = client.search(req -> req
                .index(indexName)
                .size(size)
                .from(page * size)
                .source(sc -> sc.filter(SourceFilter.of(sf -> sf
                        .includes(isNullOrEmpty(includeFields) ? Collections.emptyList() : includeFields)
                        .excludes(isNullOrEmpty(excludeFields) ? Collections.emptyList() : excludeFields)))),
                Document.class);
        System.out.println("took: " + search.took());
        return search.hits().hits().stream().map(hit -> new Document(hit.id(), hit.source())).toList();
    }

    @Override
    public List<Document> searchDocuments(String indexName, String query, int size, int page) throws IOException {
        return searchDocuments(indexName, query, size, page, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public List<Document> searchDocuments(String indexName, String searchQuery, int size, int page, List<String> includeFields, List<String> excludeFields) throws IOException {
        SearchRequest.Builder request = new SearchRequest.Builder()
                .index(indexName)
                .size(size)
                .from(page * size)
                .source(sc -> sc.filter(SourceFilter.of(sf -> sf
                        .includes(isNullOrEmpty(includeFields) ? Collections.emptyList() : includeFields)
                        .excludes(isNullOrEmpty(excludeFields) ? Collections.emptyList() : excludeFields))));

        if (!this.isNullOrEmpty(searchQuery)) {
            Query query = QueryBuilders.queryString(qs -> qs
                    .query(preprocessSearchQuery(searchQuery))
                    .defaultOperator(Operator.And)
                    .allowLeadingWildcard(true));
            request.query(query);
        }

        SearchResponse<Document> search = client.search(request.build(), Document.class);
        return search.hits().hits().stream().map(hit -> new Document(hit.id(), hit.source())).toList();
    }

    private String preprocessSearchQuery(String searchQuery) {
        searchQuery = AND_PATTERN.matcher(searchQuery).replaceAll("AND");
        searchQuery = OR_PATTERN.matcher(searchQuery).replaceAll("OR");
        searchQuery = NOT_PATTERN.matcher(searchQuery).replaceAll("NOT");
        return searchQuery;
    }

    @Override
    public SearchResponse<Document> scrollSearch(String indexName, int size, String timeout, List<String> includeFields, List<String> excludeFields) throws IOException {
        return client.search(req -> req.index(indexName)
                .size(size)
                .source(sc -> sc.filter(SourceFilter.of(sf -> sf
                        .includes(isNullOrEmpty(includeFields) ? Collections.emptyList() : includeFields)
                        .excludes(isNullOrEmpty(excludeFields) ? Collections.emptyList() : excludeFields))))
                .scroll(Time.of(t -> t.time(timeout))), Document.class);
    }

    private boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    public ScrollResponse<Document> scroll(String scrollId, String timeout) throws IOException {
        return client.scroll(req -> req.scrollId(scrollId)
                .scroll(Time.of(t -> t.time(timeout))), Document.class);
    }

    @Override
    public void clearScroll(String scrollId) throws IOException {
        client.clearScroll(req -> req.scrollId(scrollId));
    }

    @Override
    public void indexDocument(String indexName, Map<String, Object> document) {
        try {
            IndexResponse index = client.index(req -> req
                    .index(indexName)
                    .document(document));
            System.out.println("Indexed document with ID: " + index.id());
        } catch (Exception e) {
            System.err.println("Failed to index document");
            throw new RuntimeException(e);
        }
    }


}
