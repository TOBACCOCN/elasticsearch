package org.example.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;

@SpringBootTest
@Slf4j
public class RestHighLevelClientTest {

    @Autowired
    private RestHighLevelClient rhlClient;

    @Test
    public void exists() throws IOException {
        log.debug(">>>>> exists response: [{}]", rhlClient.indices().exists(new GetIndexRequest("book"), RequestOptions.DEFAULT));
    }

    @Test
    public void deleteIndex() throws IOException {
        log.debug(">>>>> deleteIndex response: [{}]", rhlClient.indices().delete(new DeleteIndexRequest("book"), RequestOptions.DEFAULT).isAcknowledged());
    }

    private String mapping() {
        return "{\n" +
                "\t\"properties\": {\n" +
                "\t\t\"author\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\"\n" +
                "\t\t},\n" +
                "\t\t\"createTime\": {\n" +
                "\t\t\t\"type\": \"date\",\n" +
                "\t\t\t\"format\": \"date_optional_time\"\n" +
                "\t\t},\n" +
                "\t\t\"id\": {\n" +
                "\t\t\t\"type\": \"keyword\"\n" +
                "\t\t},\n" +
                "\t\t\"price\": {\n" +
                "\t\t\t\"type\": \"double\"\n" +
                "\t\t},\n" +
                "\t\t\"test_field\": {\n" +
                "\t\t\t\"type\": \"text\"\n" +
                "\t\t},\n" +
                "\t\t\"title\": {\n" +
                "\t\t\t\"type\": \"text\",\n" +
                "\t\t\t\"analyzer\": \"ik_max_word\"\n" +
                "\t\t},\n" +
                "\t\t\"updateTime\": {\n" +
                "\t\t\t\"type\": \"date\",\n" +
                "\t\t\t\"format\": \"date_optional_time\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
    }

    private XContentBuilder contentBuilder() throws IOException {
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder();
        contentBuilder.startObject()
                // .startObject("mappings")         // 注意不要 mappings
                .startObject("properties")
                .startObject("id")
                .field("type", "integer")
                .endObject()
                .startObject("title")
                .field("type", "text")
                .field("analyzer", "ik_max_word")
                .endObject()
                .startObject("author")
                .field("type", "text")
                .field("analyzer", "ik_max_word")
                .endObject()
                .startObject("price")
                .field("type", "double")
                .endObject()
                .startObject("createTime")
                .field("type", "date")
                .field("format", "date_optional_time")
                .endObject()
                .startObject("updateTime")
                .field("type", "date")
                .field("format", "date_optional_time")
                .endObject()
                .endObject()
                // .endObject()
                .endObject();
        return contentBuilder;
    }

    @Test
    public void createIndex() throws IOException {
        // 注意 CreateIndexRequest 要选 org.elasticsearch.client.indices.CreateIndexRequest
        CreateIndexRequest request = new CreateIndexRequest("book");

        request.mapping(contentBuilder());      // 1.contentBuilder
        // request.mapping(mapping(), XContentType.JSON);       // 2.json

        CreateIndexResponse response = rhlClient.indices().create(request, RequestOptions.DEFAULT);
        log.debug(">>>>> createIndex response: [{}]", response.isAcknowledged());
    }

    @Test
    public void updateIndex() throws IOException {
        String srcIndex = "book";
        if (rhlClient.indices().exists(new GetIndexRequest(srcIndex), RequestOptions.DEFAULT)) {
            log.debug(">>>>> deleteIndex response: [{}]", rhlClient.indices().delete(new DeleteIndexRequest(srcIndex), RequestOptions.DEFAULT).isAcknowledged());
        }

        CreateIndexRequest request = new CreateIndexRequest("books");
        Alias alias = new Alias(srcIndex);
        alias.writeIndex(false);
        request.mapping(mapping(), XContentType.JSON).alias(alias);
        CreateIndexResponse response = rhlClient.indices().create(request, RequestOptions.DEFAULT);
        log.debug(">>>>> createIndex response: [{}]", response.isAcknowledged());
    }

    @Test
    public void index() throws Exception{
        IndexRequest request = new IndexRequest("book");
        Book book = new Book(1000, "算法导论", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein", 128.00, null, null);
        request.id(String.valueOf(book.getId()));
        ObjectMapper mapper = new ObjectMapper();
        request.source(mapper.writeValueAsString(book), XContentType.JSON);
        IndexResponse response = rhlClient.index(request, RequestOptions.DEFAULT);
        log.debug(">>>>> create response: [{}]", response);
    }

    @Test
    public void delete() throws Exception{
        DeleteRequest request = new DeleteRequest("book");
        Book book = new Book(1000, "算法导论", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein", 128.00, new Date(), null);
        request.id(String.valueOf(book.getId()));
        DeleteResponse response = rhlClient.delete(request, RequestOptions.DEFAULT);
        log.debug(">>>>> delete response: [{}]", response);
    }

    @Test
    public void update() throws Exception{
        UpdateRequest request = new UpdateRequest();
        Book book = new Book(1000, "算法导论", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein", 128.00, new Date(), null);
        request.id(String.valueOf(book.getId()));
        UpdateResponse response = rhlClient.update(request, RequestOptions.DEFAULT);
        log.debug(">>>>> delete response: [{}]", response);
    }

    @Test
    public void get() throws Exception{
        GetRequest request = new GetRequest("book");
        Book book = new Book(1000, "算法导论", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein", 128.00, new Date(), null);
        request.id(String.valueOf(book.getId()));
        GetResponse response = rhlClient.get(request, RequestOptions.DEFAULT);
        log.debug(">>>>> get response: [{}]", response);
    }

    @Test
    public void search() throws Exception{
        // queryBuilder.must(QueryBuilders.termQuery("title", "算法导论"))
        //         .must(QueryBuilders.termQuery("author", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein"));
        // queryBuilder.must(QueryBuilders.matchQuery("title", "导论").fuzziness(Fuzziness.AUTO))
        //         .must(QueryBuilders.termQuery("author", "Thomas H.Cormen / Charles E.Leiserson / Ronald L.Rivest / Clifford Stein"));

        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 排序分页
        builder.sort("test_field", SortOrder.ASC);      // 排序必须是 keyword 类型的字段
        builder.from(0);
        builder.size(10);

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field filedBuilder = new HighlightBuilder.Field("title");
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        highlightBuilder.field(filedBuilder);
        builder.highlighter(highlightBuilder);

        // builder.query(QueryBuilders.matchAllQuery());       // 1.matchAll
        // builder.query(QueryBuilders.matchQuery("author", "Thomas H.Cormen"));       // 2.matchQuery, 设置了分词的字段好像必须使用 matchQuery, 即使是英文. termQuery 则查不到数据
        // builder.query(QueryBuilders.matchQuery("title", "导论"));       // 3.matchQuery, 设置了分词的字段好像必须使用 matchQuery. termQuery 则查不到数据
        // builder.query(QueryBuilders.termQuery("test_field", "hello"));       // 4.termQuery, 如果字段类型为 text 也会模糊匹配, 设置类型成 keyword 才会精确匹配

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.matchQuery("title", "深入理解"))
                .should(QueryBuilders.termQuery("test_field", "hello"));
        builder.query(queryBuilder);

        SearchRequest request = new SearchRequest("books");
        request.source(builder);
        SearchResponse response = rhlClient.search(request, RequestOptions.DEFAULT);
        log.debug(">>>>> search response: [{}]", response);
    }

}
