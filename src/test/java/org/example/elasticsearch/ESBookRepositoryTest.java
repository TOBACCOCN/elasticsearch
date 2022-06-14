package org.example.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
public class ESBookRepositoryTest {

    @Autowired
    private ESBookRepository esBookRepository;

    @Test
    public void save() {
        // Book book = new Book(12345, "ElasticSearch权威指南", "elasticsearch", 88.8,
        //         new Date(), new Date());
        Book book = new Book(12346, "深入理解 Java 虚拟机", "周志明", 69.00,
                new Date(), new Date());
        log.debug(">>>>> book: [{}]", book);
        esBookRepository.save(book);
    }

    @Test
    public void findAll() {
        Iterable<Book> books = esBookRepository.findAll();
        books.forEach(book -> log.debug(">>>>> book: [{}]", book));
    }

    @Test
    public void findByTitleOrAuthor() {
        log.debug(">>>>> books: [{}]", esBookRepository.findByTitleOrAuthor("指南", "elasticsearch"));
    }

    @Test
    public void findByTitle() {
        log.debug(">>>>> books: [{}]", esBookRepository.findByTitle("指南"));
    }

    @Test
    public void findByAuthor() {
        log.debug(">>>>> books: [{}]", esBookRepository.findByAuthor("elasticsearch"));
    }

    @Test
    public void update() {
        Iterable<Book> books = esBookRepository.findAll();
        books.forEach(book -> book.setTitle(book.getTitle().toUpperCase()));
        esBookRepository.saveAll(books);
    }

    @Test
    public void delete() {
        Book book = new Book();
        book.setId(12345);
        esBookRepository.delete(book);
    }

}
