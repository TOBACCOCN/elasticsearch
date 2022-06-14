package org.example.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpHeaders;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
@Slf4j
public class ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.nodes}")
    private String elasticsearchNodes;
    @Value("${spring.data.elasticsearch.username}")
    private String username;
    @Value("${spring.data.elasticsearch.password}")
    private String password;

    // @Bean
    public RestClient restClient() {
        HttpHost[] hosts = Arrays.stream(elasticsearchNodes.split(","))
                .map(httpPort -> new HttpHost(httpPort.split(":")[0], Integer.parseInt(httpPort.split(":")[1]), "https")).toArray(HttpHost[]::new);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        // Create the low-level client
        RestClient restClient = RestClient.builder(hosts).setHttpClientConfigCallback(builder -> {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
                sslContext.init(null, trustManagers, null);
                return builder.setDefaultCredentialsProvider(credentialsProvider).setSSLContext(sslContext);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }).build();
        log.debug(">>>>> RestClient  Initialization completed");
        return restClient;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // Create the HLRC with the low level client
        RestHighLevelClient restHighLevelClient = new RestHighLevelClientBuilder(restClient())
                .setApiCompatibilityMode(true)
                .build();
        log.debug(">>>>> RestHighLevelClient  Initialization completed");
        return restHighLevelClient;
    }

    // 不太好用
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // Create the Java API Client with the low level client
        ElasticsearchTransport transport = new RestClientTransport(restClient(), new JacksonJsonpMapper());
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);
        log.debug(">>>>> ElasticsearchClient  Initialization completed");
        return elasticsearchClient;
    }

}
