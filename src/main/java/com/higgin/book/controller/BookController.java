package com.higgin.book.controller;


import com.higgin.book.BookServiceAppApplication;
import com.higgin.book.domain.Book;
import com.higgin.book.service.BookProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/book")
public class BookController {


    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    BookProcessService bookService;

    BookController(BookProcessService bookService){
        this.bookService = bookService ;
    }

    @GetMapping("/name")
    public Optional<List<Book>> searchBook(@RequestParam  String bookName){
        LOGGER.info("search book with actuator {} ",bookName);
        List<Book> bookList =  bookService.findBookService(bookName) ;
        return bookList.isEmpty() ? Optional.empty() : Optional.of(bookList);
    }


    @PostMapping("/save")
    public void saveBook(@RequestBody Book book){
        LOGGER.info("save book");
        bookService.saveBookService(book);
    }
}
