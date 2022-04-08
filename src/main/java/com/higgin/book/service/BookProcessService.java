package com.higgin.book.service;

import com.higgin.book.controller.BookController;
import com.higgin.book.domain.Book;
import com.higgin.book.repo.BookRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookProcessService.class);

    ArrayList<Book> bookList = new ArrayList<Book>();

    @Autowired
    BookRepo bookRepo;

    /**
     * @param book
     */
    public void saveBookService(Book book) {
        LOGGER.info("Books are added to the list successfully");
        //   bookList.add(book);
        //save the book to repo.
        bookRepo.save(book);
    }


    /**
     * @param bookName
     * @return
     */
    public Optional<Book> findBookById(String bookName) {
        LOGGER.info("Books are retrieved from the list successfully");
        return bookRepo.findById(Long.parseLong(bookName));
    }


    /**
     *
     * @return
     */
    public List<Book> getAllBooks() {
        LOGGER.info("Get all the books executed");
        return bookRepo.findAll();
    }

}
