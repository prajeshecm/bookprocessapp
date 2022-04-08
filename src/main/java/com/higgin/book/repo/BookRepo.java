package com.higgin.book.repo;

import com.higgin.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookRepo extends JpaRepository<Book, Long> {
   //TODO : implement all the book repo methods which we needs here  , like below.

   //List<Book> findByPublished(boolean published);
   //List<Book> findByTitleContaining(String bookName);
}
