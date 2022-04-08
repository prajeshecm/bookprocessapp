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

    @PostMapping("/save")
    public void saveBook(@RequestBody Book book){
        LOGGER.info("save book");
        bookService.saveBookService(book);
    }

    @GetMapping("/search")
    public Optional<Book> searchBook(@RequestParam  String bookId){
        LOGGER.info("search book with actuator {} ",bookId);
        return  bookService.findBookById(bookId) ;
    }


    @GetMapping("/all")
    public Optional<List<Book>> getAllBooks(){
        LOGGER.info("get all the books from repo");
        List<Book> bookList =  bookService.getAllBooks() ;
        return bookList.isEmpty() ? Optional.empty() : Optional.of(bookList);
    }
}
