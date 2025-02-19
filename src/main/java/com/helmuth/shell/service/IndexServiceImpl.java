package com.helmuth.shell.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.*;
import com.helmuth.shell.model.Document;
import com.helmuth.shell.model.IndexDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class IndexServiceImpl implements IndexService<Document> {
    private static final Logger log = LoggerFactory.getLogger(IndexServiceImpl.class);
    private final ElasticsearchClient client;

    public IndexServiceImpl(ElasticsearchClient client) {
        this.client = client;
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
    public void indexDocument(Document document) {
        try {
            IndexResponse index = client.index(req -> req
                    .index(getIndexName(document))
                    .document(document));
            System.out.println("Indexed document with ID: " + index.id());
        } catch (Exception e) {
            System.err.println("Failed to index document");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void indexDocuments(Collection<Document> documents) throws IOException {
        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        for (Document document : documents) {
            bulkRequest.operations(op -> op.index(i -> i.index(getIndexName(document)).document(document)));
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
    public List<Document> getDocuments(String indexName) throws IOException {
        SearchResponse<Document> search = client.search(req -> req.index(indexName), Document.class);
        return search.hits().hits().stream().map(hit -> new Document(hit.id(), hit.source())).toList();

    }

    @Override
    public SearchResponse<Document> scrollSearch(String indexName, int size, String timeout) throws IOException {
        return client.search(req -> req.index(indexName)
                .size(size)
                .scroll(Time.of(t -> t.time(timeout))), Document.class);
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


    private String getIndexName(Document document) {
        IndexDocument annotation = document.getClass().getAnnotation(IndexDocument.class);
        if (annotation != null) {
            return annotation.indexName();
        }
        throw new IllegalArgumentException("Document is not annotated with @IndexDocument");
    }
}

