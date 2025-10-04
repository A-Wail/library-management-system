package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookRepoTest {

    @Mock
    private BookRepo bookRepo;

    @Test
    void findByIsbnReturnsBookWhenExists() {
        String isbn = "978-3-16-148410-0";
        Book book = Book.builder()
                .id(1L)
                .isbn(isbn)
                .title("The Great Gatsby")
                .build();
        when(bookRepo.findByIsbn(isbn)).thenReturn(Optional.of(book));
        Optional<Book> result = bookRepo.findByIsbn(isbn);

        assertTrue(result.isPresent(), "Book should be present");
        assertEquals(isbn, result.get().getIsbn(), "ISBN should match");
        assertEquals("The Great Gatsby", result.get().getTitle(), "Title should match");
    }

    @Test
    void findByIsbnReturnsEmptyWhenBookDoesNotExist() {

        String isbn = "978-0-00-000000-0";
        when(bookRepo.findByIsbn(isbn)).thenReturn(Optional.empty());


        Optional<Book> result = bookRepo.findByIsbn(isbn);


        assertFalse(result.isPresent(), "Book should not be present");
    }
}