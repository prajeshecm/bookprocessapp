package com.higgin.book.service;

import com.higgin.book.controller.BookController;
import com.higgin.book.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookProcessService.class);

    ArrayList<Book> bookList = new ArrayList<Book>();

    /**
     * @param book
     */
    public void saveBookService(Book book) {
        LOGGER.info("Books are added to the list successfully");
        bookList.add(book);
    }


    /**
     * @param bookName
     * @return
     */
    public List<Book> findBookService(String bookName) {
        LOGGER.info("Books are retrived from the list successfully");
        return bookList.stream().filter(bookVal -> bookVal.getBookName().equalsIgnoreCase(bookName)).collect(Collectors.toList());
    }

/*
    public static void main(String[] args) {
        testStream();
    }

    private static void testStream() {

        ArrayList<String> stringList = new ArrayList<String>();
        stringList.add("Rama");
        stringList.add("krishna");
        stringList.add("Hare");
        stringList.add("Ramaa");
        stringList.add("Hare2");

        List updatedList = Stream.of(stringList).filter(abc -> abc.remove("Harewewwe")).collect(Collectors.toList());


        Boolean isExist = Stream.of(stringList).equals("krishna");


        Boolean isExist2 = stringList.stream().anyMatch(e-> e.contains("Hare2"));


        Boolean isExist3 = boolean found = stringList.stream()
                .anyMatch(p -> p.name.equals(""));



    }*/
}
