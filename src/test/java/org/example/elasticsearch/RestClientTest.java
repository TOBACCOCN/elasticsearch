package org.example.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Slf4j
public class RestClientTest {

    @Autowired
    private RestClient restClient;

    private void printResponse(Response response) throws IOException {
        log.debug(">>>>> requestLine: [{}], host: [{}], statusCode: [{}], headers: [{}], responseBody: [{}]",
                response.getRequestLine(), response.getHost(), response.getStatusLine().getStatusCode(),
                response.getHeaders(), EntityUtils.toString(response.getEntity()));
    }

    private Request request(String method, String endpoint, Map<String, String> parameters) {
        Request request = new Request(method, endpoint);
        if (parameters != null && parameters.size() > 0) {
            parameters.forEach(request::addParameter);
        }
        return request;
    }

    private Map<String, String> pretty() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("pretty", "true");
        return parameters;
    }

    @Test
    public void delete() throws IOException {
        // 1.删除索引
        // printResponse(restClient.performRequest(request("DELETE", "/book", pretty())));

        // 2.删除 document
        String id = "1000";
        printResponse(restClient.performRequest(request("DELETE", "/book/_doc/" + id, pretty())));
    }

    @Test
    public void get() throws IOException {
        // 1.查询
        // printResponse(restClient.performRequest(request("GET", "/", pretty())));

        // 2. 查询索引
        printResponse(restClient.performRequest(request("GET", "/book", pretty())));

        // 3.查询文档
        printResponse(restClient.performRequest(request("GET", "/book/_search", pretty())));
    }

    @Test
    public void getAsync() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        restClient.performRequestAsync(request("GET", "/", pretty()), new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                log.debug(">>>>> onSuccess");
                try {
                    printResponse(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                log.debug(">>>>> onFailure: [{}]", e.getMessage());
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    @Test
    public void put() throws IOException {
        // 1. 添加索引
        Request request = request("PUT", "/book", pretty());
        JSONObject propertyJsonObj = new JSONObject();

        JSONObject idJsonObj = new JSONObject();
        idJsonObj.put("type", "keyword");
        propertyJsonObj.put("id", idJsonObj);

        JSONObject titleJsonObj = new JSONObject();
        titleJsonObj.put("type", "text");
        titleJsonObj.put("analyzer", "ik_max_word");

        propertyJsonObj.put("title", titleJsonObj);

        JSONObject authorJsonObj = new JSONObject();
        authorJsonObj.put("type", "text");
        authorJsonObj.put("analyzer", "ik_max_word");
        propertyJsonObj.put("author", authorJsonObj);

        JSONObject priceJsonObj = new JSONObject();
        priceJsonObj.put("type", "double");
        propertyJsonObj.put("price", priceJsonObj);

        JSONObject createTimeJsonObj = new JSONObject();
        createTimeJsonObj.put("type", "date");
        createTimeJsonObj.put("format", "date_optional_time");
        propertyJsonObj.put("createTime", createTimeJsonObj);

        JSONObject updateTimeJsonObj = new JSONObject();
        updateTimeJsonObj.put("type", "date");
        updateTimeJsonObj.put("format", "date_optional_time");
        propertyJsonObj.put("updateTime", updateTimeJsonObj);

        JSONObject mappingJsonObj = new JSONObject();
        mappingJsonObj.put("properties", propertyJsonObj);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mappings", mappingJsonObj);

        request.setJsonEntity(JSON.toJSONString(jsonObject));
        printResponse(restClient.performRequest(request));

        // 2.添加/修改 document
        // String id = "1000";
        // Request request = request("PUT", "/book/_doc/" + id, pretty());
        // // request.setJsonEntity("{\"title\":\"Elasticsearch 权威指南\"}");     // 1.添加时用
        // request.setJsonEntity("{\"title\":\"ELASTICSEARCH 权威指南\", \"price\": 79.80}");    // 2.更新时用
        // printResponse(restClient.performRequest(request));
    }

    @Test
    public void post() throws IOException {
        // 添加/修改 document

        // Request request = request("POST", "/book/_doc", pretty());      // 1.没有 ID 时是添加, ES 会自动生成 id

        String id = "1000";
        Request request = request("POST", "/book/_doc/" + id, pretty());      // 2.有 ID: 跟已有的 ID 不重复时是添加, 重复时是修改

        request.setEntity(new NStringEntity("{\"title\":\"ELASTICSEARCH 权威指南\", \"price\": 0.00}", ContentType.APPLICATION_JSON));
        printResponse(restClient.performRequest(request));
    }

}
