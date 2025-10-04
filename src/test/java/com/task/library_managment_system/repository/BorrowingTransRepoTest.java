package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.BorrowingTransaction;
import com.task.library_managment_system.models.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowingTransRepoTest {
    @Mock private BorrowingTransRepo transRepo;

    @Test
    void findByBookIdReturnsTransactionsWhenExists() {
        Long bookId = 1L;
        Book book = Book.builder()
                .id(bookId)
                .isbn("978-3-16-148410-0")
                .title("The Great Gatsby")
                .build();
        Member member = Member.builder()
                .id(1L)
                .name("John Doe")
                .build();
        BorrowingTransaction transaction1 = BorrowingTransaction.builder()
                .id(1L)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.of(2025, 9, 25))
                .dueDate(LocalDate.of(2025, 10, 9))
                .status(BorrowingTransaction.transactionState.BORROWED)
                .build();
        BorrowingTransaction transaction2 = BorrowingTransaction.builder()
                .id(2L)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.of(2025, 9, 20))
                .dueDate(LocalDate.of(2025, 10, 4))
                .status(BorrowingTransaction.transactionState.RETURNED)
                .build();
        List<BorrowingTransaction> transactions = Arrays.asList(transaction1, transaction2);
        when(transRepo.findByBookId(bookId)).thenReturn(transactions);

        // When
        List<BorrowingTransaction> result = transRepo.findByBookId(bookId);

        // Assert
        assertEquals(2, result.size(), "Should return two transactions");
        assertEquals(bookId, result.get(0).getBook().getId(), "Book ID should match");
        assertEquals(BorrowingTransaction.transactionState.BORROWED, result.get(0).getStatus(), "Status should be BORROWED");
        assertEquals(BorrowingTransaction.transactionState.RETURNED, result.get(1).getStatus(), "Status should be RETURNED");
    }

    @Test
    void findByBookIdReturnsTransactionsWhenNotExists(){
        // Arrange
        Long bookId = 2L;
        when(transRepo.findByBookId(bookId)).thenReturn(Collections.emptyList());

        // Act
        List<BorrowingTransaction> result = transRepo.findByBookId(bookId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list");
    }
}