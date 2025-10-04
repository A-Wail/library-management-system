package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.borrow.BorrowingResponse;
import com.task.library_managment_system.dto.borrow.ReturnedResponse;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.book.BookNotAvailableNowException;
import com.task.library_managment_system.exception.transaction.BookAlreadyReturnedException;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.BorrowingTransaction;
import com.task.library_managment_system.models.Member;
import com.task.library_managment_system.models.Publisher;
import com.task.library_managment_system.repository.BookRepo;
import com.task.library_managment_system.repository.BorrowingTransRepo;
import com.task.library_managment_system.repository.MemberRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class BorrowingServiceImplTest {
    @Mock private BorrowingTransRepo transRepo;
    @Mock private MemberRepo memberRepo;
    @Mock private BookRepo bookRepo;
    @InjectMocks private BorrowingServiceImpl borrowingService;
    private Member member;
    private Book book;
    private BorrowingTransaction transaction;

    @BeforeEach
    void setUp() {
        member=Member.builder()
                .id(1L)
                .name("Sad Ahmed")
                .email("sad.ahmed@example.com")
                .phone("0102222222")
                .membershipDate(LocalDate.of(2025,9,25))
                .transactions(Collections.emptyList())
                .build();
        book= Book.builder()
                .id(1L)
                .isbn("1234567890")
                .title("Test Book")
                .edition("1st")
                .language("English")
                .coverUrl("http://example.com/cover.jpg")
                .summary("A test book summary")
                .publicationYear(2020)
                .publisher(new Publisher())
                .categories(Collections.emptyList())
                .authors(Collections.emptyList())
                .build();
        transaction=BorrowingTransaction.builder()
                .id(1L)
                .member(member)
                .book(book)
                .borrowDate(LocalDate.of(2025,9,20))
                .status(BorrowingTransaction.transactionState.BORROWED)
                .dueDate(LocalDate.of(2025,10,1))
                .build();
    }

    @Test
    void borrowBookSuccess() {
        //When
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(transRepo.findByBookId(1L)).thenReturn(List.of());
        when(transRepo.save(any(BorrowingTransaction.class))).thenReturn(transaction);
        //Then
        BorrowingResponse response=borrowingService.borrowBook(1L,1L);
        //Assert
        assertNotNull(response,"Transaction should not be null!");
        assertEquals("Sad Ahmed",response.getMemberName(),"Name of member should match!");
        assertEquals("Test Book",response.getBookTitle(),"Title book should match!");
        verify(memberRepo,times(1)).findById(1L);
        verify(bookRepo,times(1)).findById(1L);
        verify(transRepo,times(1)).findByBookId(1L);
        verify(transRepo,times(1)).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_throwEntityNotFoundException_whenMemberNotExist() {
        //When
        when(memberRepo.findById(1L)).thenReturn(Optional.empty());
        //Then
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->borrowingService.borrowBook(1L,1L),
                "Should throw exception");
        //Assert
        assertEquals("No member with this id:1",notFoundException.getMessage());
        verify(memberRepo,times(1)).findById(1L);
        verify(bookRepo,never()).findById(1L);
        verify(transRepo,never()).findByBookId(1L);
        verify(transRepo,never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_throwEntityNotFoundException_whenBookNotExist() {
        //When
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepo.findById(1L)).thenReturn(Optional.empty());
        //Then
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->borrowingService.borrowBook(1L,1L),
                "Should throw exception");
        //Assert
        assertEquals("No book with this id:1",notFoundException.getMessage());
        verify(memberRepo,times(1)).findById(1L);
        verify(bookRepo,times(1)).findById(1L);
        verify(transRepo,never()).findByBookId(1L);
        verify(transRepo,never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_throwBookNotAvailableNowException_whenBookNotAvailable() {
        //When
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(transRepo.findByBookId(1L)).thenReturn(List.of(transaction));
        //Then
        BookNotAvailableNowException notFoundException=assertThrows(BookNotAvailableNowException.class,
                ()->borrowingService.borrowBook(1L,1L),
                "Should throw exception");
        //Assert
        assertEquals("Can't borrow this book Test Book because it borrowed now !!",notFoundException.getMessage());
        verify(memberRepo,times(1)).findById(1L);
        verify(bookRepo,times(1)).findById(1L);
        verify(transRepo,times(1)).findByBookId(1L);
        verify(transRepo,never()).save(any(BorrowingTransaction.class));
    }


    @Test
    void getBorrowingByIdSuccess_withReturnedState() {
        //Given
        BorrowingTransaction updatedTransaction= BorrowingTransaction.builder()
                .id(1L)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.of(2025,9,20))
                .returnDate(LocalDate.of(2025,9,28))
                .dueDate(LocalDate.of(2025,10,10))
                .status(BorrowingTransaction.transactionState.RETURNED)
                .build();
        transaction.setDueDate(LocalDate.of(2025,10,10));
        //when
        when(transRepo.findById(1L)).thenReturn(Optional.of(transaction));
        when(transRepo.save(any(BorrowingTransaction.class))).thenReturn(updatedTransaction);
        //Then & Assert
        ReturnedResponse response=borrowingService.getBorrowingById(1L);

        assertNotNull(response,"Returned response not be null");
        assertEquals("RETURNED",response.getStatus(),"Status should match");
        assertEquals("Test Book",response.getBookTitle(),"Book title should match");
        assertEquals("Sad Ahmed",response.getMemberName(),"Name should be match");
        assertEquals(1L,response.getTransactionId(),"ID should be match");
        verify(transRepo,times(1)).findById(1L);
        verify(transRepo,times(1)).save(any(BorrowingTransaction.class));
    }

    @Test
    void getBorrowingByIdSuccess_withOverdueState() {
        //Given
        BorrowingTransaction updatedTransaction= BorrowingTransaction.builder()
                .id(1L)
                .book(book)
                .member(member)
                .borrowDate(LocalDate.of(2025,9,20))
                .returnDate(LocalDate.of(2025,10,15))
                .dueDate(LocalDate.of(2025,10,10))
                .status(BorrowingTransaction.transactionState.OVERDUE)
                .build();
        //when
        when(transRepo.findById(1L)).thenReturn(Optional.of(transaction));
        when(transRepo.save(any(BorrowingTransaction.class))).thenReturn(updatedTransaction);
        //Then & Assert
        ReturnedResponse response=borrowingService.getBorrowingById(1L);

        assertNotNull(response,"Returned response not be null");
        assertEquals("OVERDUE",response.getStatus(),"Status should match");
        assertEquals("Test Book",response.getBookTitle(),"Book title should match");
        assertEquals("Sad Ahmed",response.getMemberName(),"Name should be match");
        assertEquals(1L,response.getTransactionId(),"ID should be match");
        verify(transRepo,times(1)).findById(1L);
        verify(transRepo,times(1)).save(any(BorrowingTransaction.class));
    }

    @Test
    void getBorrowingById_throwEntityNotFoundException_whenTransactionNotExist() {
        //when
        when(transRepo.findById(1L)).thenReturn(Optional.empty());
        //Then & Assert
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->borrowingService.getBorrowingById(1L),
                "Should throw notFoundException");

        assertEquals("can't find transaction with id:1",notFoundException.getMessage());
        verify(transRepo,times(1)).findById(1L);
        verify(transRepo,never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void getBorrowingById_throwBookAlreadyReturnedException_whenBookAlreadyReturned() {
        //Given
        transaction.setStatus(BorrowingTransaction.transactionState.RETURNED);
        //when
        when(transRepo.findById(1L)).thenReturn(Optional.of(transaction));
        //Then & Assert
        BookAlreadyReturnedException alreadyReturnedException=assertThrows(BookAlreadyReturnedException.class,
                ()->borrowingService.getBorrowingById(1L),
                "Should throw bookAlreadyReturnedException");

        assertEquals("Book already returned: 1",alreadyReturnedException.getMessage());
        verify(transRepo,times(1)).findById(1L);
        verify(transRepo,never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void isBookCurrentlyBorrowed_shouldReturnTrue_whenActiveTransactionExists() {
        when(transRepo.findByBookId(1L)).thenReturn(List.of(transaction));
        assertTrue(borrowingService.isBookCurrentlyBorrowed(1L));
    }

    @Test
    void isBookCurrentlyBorrowed_shouldReturnFalse_whenNoActiveTransaction() {
        when(transRepo.findByBookId(1L)).thenReturn(List.of());
        assertFalse(borrowingService.isBookCurrentlyBorrowed(1L));
    }
}