package org.example.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Hello world!
 */
@SpringBootApplication
public class ElasticserachStarter {
    public static void main(String[] args) {
        SpringApplication.run(ElasticserachStarter.class, args);
    }
}
