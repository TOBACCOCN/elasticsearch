package org.example.elasticsearch;


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

// 好像必须要 RestHighLevelClient(ES 开启 xpack.security 后同时开启 https, 摸索半天程序一直起不来，报错 nested: ConnectionClosedException[Connection is closed];)
// 后来碰巧根据官网配置一些旧的过时 client, 也就是 RestHighLevelClient, 突然发现不报错了, 该 Repository 对应 domain-class 中配置的自动创建索引也在程序启动后创建了索引
public interface ESBookRepository extends ElasticsearchRepository<Book, Integer> {

    List<Book> findByTitleOrAuthor(String title, String author);

    List<Book> findByTitle(String title);

    List<Book> findByAuthor(String author);

}
