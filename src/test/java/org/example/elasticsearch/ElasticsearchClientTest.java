package org.example.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.Alias;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 不太好用
@SpringBootTest
@Slf4j
public class ElasticsearchClientTest {

    @Autowired
    private ElasticsearchClient esClient;

    @Test
    public void createIndex() throws IOException {
        // deleteIndex();
        CreateIndexResponse response = esClient.indices().create(builder -> builder.index("book"));
        log.debug(">>>>> createIndex response: [{}]", response.acknowledged());
    }

    @Test
    public void getIndex() throws IOException {
        GetIndexResponse response = esClient.indices().get(builder -> builder.index("book"));
        log.debug(">>>>> getIndex response: [{}]", JSON.toJSONString(response.result().get("book")));
    }

    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexResponse response = esClient.indices().delete(builder -> builder.index("book"));
        log.debug(">>>>> deleteIndex response: [{}]", response.acknowledged());
    }

    @Test
    public void create() throws IOException {
        // Book book = new Book("234235", "ElasticSearch权威指南", "elasticsearch", 88.8d,
        //         new Date(), null);
        Book book = new Book(234235, "ElasticSearch 权威指南", "中华人民共和国", 88.8d,
                new Date(), null);
        CreateResponse response = esClient.create(builder -> builder.index("book").id("1001").document(book));
        log.debug(">>>>> create response: [{}]", JSON.toJSONString(response.result()));
    }

    @Test
    public void get() throws IOException {
        GetResponse<Book> response = esClient.get(builder -> builder.index("book").id("1000"), Book.class);
        log.debug(">>>>> get response: [{}]", JSON.toJSONString(response.source()));
    }

    @Test
    public void search() throws IOException {
        SearchResponse<Book> response = esClient.search(builder -> builder.index("book")
                .query(queryBuilder -> queryBuilder.term(termQuery -> termQuery.field("author").value("共和国"))), Book.class);

        // title 和 author 字段使用了 ik 分词器, 所以 title = "指南" 或 author = "共和国" 都能查到数据
        // SearchResponse<Book> response = esClient.search(builder -> builder.index("book")
        //         .query(queryBuilder -> queryBuilder.term(termQuery -> termQuery.field("title").value("指南"))), Book.class);
        // SearchResponse<Book> response = esClient.search(builder -> builder.index("book")
        //         .query(queryBuilder -> queryBuilder.term(termQuery -> termQuery.field("author").value("共和国"))), Book.class);

        List<Hit<Book>> hits = response.hits().hits();
        hits.forEach(hit -> {
            log.debug(">>>>> search response: [{}]", JSON.toJSONString(hit.source()));
        });
    }

    @Test
    public void update() throws IOException {
        Book book = new Book(234234, "ElasticSearch权威指南", "ELASTICSEARCH", 88.8d,
                new Date(), null);
        UpdateResponse<Book> response = esClient.update(builder -> builder.index("book").id("1000").doc(book), Book.class);
        // Map<String, Object> map = new HashMap<>();
        // map.put("author", "ELASTICSEARCH");
        // UpdateResponse<Book> response = esClient.update(builder -> builder.index("book").id("1000").doc(map), Book.class);
        log.debug(">>>>> update response: [{}]", JSON.toJSONString(response.result()));
    }

    @Test
    public void delete() throws IOException {
        DeleteResponse response = esClient.delete(builder -> builder.index("book").id("1000"));
        log.debug(">>>>> delete response: [{}]", JSON.toJSONString(response.result()));
    }

}
