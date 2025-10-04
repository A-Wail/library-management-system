package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.book.OverviewBook;
import com.task.library_managment_system.dto.book.RequestBook;
import com.task.library_managment_system.dto.book.ShowBook;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.book.BookNotAvailableNowException;
import com.task.library_managment_system.models.Author;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.Category;
import com.task.library_managment_system.models.Publisher;
import com.task.library_managment_system.repository.AuthorRepo;
import com.task.library_managment_system.repository.BookRepo;
import com.task.library_managment_system.repository.CategoryRepo;
import com.task.library_managment_system.repository.PublisherRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock   private  BookRepo bookRepo;
    @Mock   private  AuthorRepo authorRepo;
    @Mock   private  CategoryRepo categoryRepo;
    @Mock   private  PublisherRepo publisherRepo;
    @Mock   private  BorrowingService borrowingService;
    @InjectMocks BookServiceImpl bookService;
    private Book book;
    private RequestBook requestBook;
    private Publisher publisher;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        publisher = Publisher.builder().id(1L).name("Dar Alnashr").build();
        author = Author.builder().id(1L).name("John Doe").build();
        category = Category.builder().id(1L).name("Fiction").build();
        book = Book.builder()
                .id(1L)
                .isbn("1234567890")
                .title("Test Book")
                .edition("1st")
                .language("English")
                .coverUrl("http://example.com/cover.jpg")
                .summary("A test book summary")
                .publicationYear(2020)
                .publisher(publisher)
                .categories(List.of(category))
                .authors(List.of(author))
                .build();
        requestBook = RequestBook.builder()
                .isbn("1234567890")
                .title("Test Book")
                .edition("1st")
                .language("English")
                .coverUrl("http://example.com/cover.jpg")
                .summary("A test book summary")
                .publicationYear(2020)
                .build();
    }


    @Test
    void createBookSuccess() {
        //given
        List<Long> categoryIds=List.of(1L);
        List<Long> authorIds=List.of(1L);
        Long publisherId=1L;
        //when
        when(bookRepo.findByIsbn(requestBook.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(categoryIds)).thenReturn(List.of(category));
        when(publisherRepo.findById(publisherId)).thenReturn(Optional.of(publisher));
        when(authorRepo.findAllById(authorIds)).thenReturn(List.of(author));
        when(bookRepo.save(any(Book.class))).thenReturn(book);
        //then & assert
        ShowBook response =bookService.createBook(requestBook,categoryIds,publisherId,authorIds);

        assertNotNull(response, "Response should not be null");
        assertEquals("Test Book", response.getTitle(), "Title should match");
        assertEquals("1234567890", response.getIsbn(), "ISBN should match");
        assertEquals(List.of("Fiction"), response.getCategoriesName(), "Categories should match");
        assertEquals(List.of(1L), response.getAuthorIds(), "Author IDs should match");
        assertEquals(1L, response.getPublisherId(), "Publisher ID should match");
        verify(bookRepo, times(1)).findByIsbn("1234567890");
        verify(categoryRepo, times(1)).findAllById(categoryIds);
        verify(publisherRepo, times(1)).findById(publisherId);
        verify(authorRepo, times(1)).findAllById(authorIds);
        verify(bookRepo, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_throwEntityFoundException_whenBookExist() {
        //given
        List<Long> categoryIds=List.of(1L);
        List<Long> authorIds=List.of(1L);
        Long publisherId=1L;
        //when
        when(bookRepo.findByIsbn(requestBook.getIsbn())).thenReturn(Optional.of(book));
        //then & assert
        EntityFoundException exception=assertThrows(EntityFoundException.class,
                ()->bookService.createBook(requestBook,categoryIds,publisherId,authorIds),
                "should throw EntityFoundException!");
        assertEquals("This book is already exist with isbn: 1234567890",exception.getMessage());
        verify(bookRepo, times(1)).findByIsbn("1234567890");
        verify(categoryRepo, never()).findAllById(categoryIds);
        verify(publisherRepo,never()).findById(publisherId);
        verify(authorRepo, never()).findAllById(authorIds);
        verify(bookRepo, never()).save(any(Book.class));
    }

    @Test
    void createBook_throwEntityNotFoundException_whenCategoriesNotFound() {
        //given
        List<Long> categoryIds=List.of(1L,2L);
        //when
        when(bookRepo.findByIsbn(requestBook.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(categoryIds)).thenReturn(List.of(category));
        //then & assert
        EntityNotFoundException exception=assertThrows(EntityNotFoundException.class,
                ()->bookService.createBook(requestBook,categoryIds,1L,List.of(1L)),
                "should throw EntityNotFoundException because categories not found");

        assertEquals("One or more categories not found",exception.getMessage());
        verify(bookRepo, times(1)).findByIsbn("1234567890");
        verify(categoryRepo, times(1)).findAllById(categoryIds);
        verify(publisherRepo,never()).findById(1L);
        verify(authorRepo, never()).findAllById(List.of(1L));
        verify(bookRepo, never()).save(any(Book.class));
    }

    @Test
    void createBook_throwEntityNotFoundException_whenPublisherNotFound() {
        //when
        when(bookRepo.findByIsbn(requestBook.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(publisherRepo.findById(1L)).thenReturn(Optional.empty());
        //then & assert
        EntityNotFoundException exception=assertThrows(EntityNotFoundException.class,
                ()->bookService.createBook(requestBook,List.of(1L),1L,List.of(1L)),
                "should throw EntityNotFoundException because publisher not found");

        assertEquals("Publisher Not found with id:1",exception.getMessage());
        verify(bookRepo, times(1)).findByIsbn("1234567890");
        verify(categoryRepo, times(1)).findAllById(List.of(1L));
        verify(publisherRepo,times(1)).findById(1L);
        verify(authorRepo, never()).findAllById(List.of(1L));
        verify(bookRepo, never()).save(any(Book.class));
    }

    @Test
    void createBook_throwEntityNotFoundException_whenAuthorsNotFound() {
        //given
        List<Long> categoryIds=List.of(1L);
        List<Long> authorIds=List.of(1L,2L);
        //when
        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(categoryIds)).thenReturn(List.of(category));
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepo.findAllById(authorIds)).thenReturn(List.of(author));
        //then & assert
        EntityNotFoundException exception=assertThrows(EntityNotFoundException.class,
                ()->bookService.createBook(requestBook,categoryIds,1L,authorIds),
                "should throw EntityNotFoundException because authors not found");

        assertEquals("One or more authors not found",exception.getMessage());
        verify(bookRepo, times(1)).findByIsbn("1234567890");
        verify(categoryRepo, times(1)).findAllById(categoryIds);
        verify(publisherRepo,times(1)).findById(1L);
        verify(authorRepo, times(1)).findAllById(authorIds);
        verify(bookRepo, never()).save(any(Book.class));
    }


    @Test
    void searchBookByIsbnSuccess() {
        //when
        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.of(book));
        //then & assert
        ShowBook response=bookService.searchBookByIsbn("1234567890");
        assertNotNull(response,"Book shouldn't be null!");
        assertEquals("1234567890",response.getIsbn(),"ISBN should match!");
        assertEquals("Test Book", response.getTitle(), "Title should match");
        verify(bookRepo,times(1)).findByIsbn("1234567890");

    }

    @Test
    void searchBookByIsbn_throwEntityNotFoundException_whenBookNotExist() {
        //when
        when(bookRepo.findByIsbn("1234567890")).thenReturn(Optional.empty());
        //then & assert
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->bookService.searchBookByIsbn("1234567890"),
                "should throw EntityNotFoundException because book not exist");
        assertEquals("Book not found with isbn:1234567890", notFoundException.getMessage());
        verify(bookRepo,times(1)).findByIsbn("1234567890");
    }

    @Test
    void overviewBooksSuccess() {
        //when
        when(bookRepo.findAll()).thenReturn(List.of(book));
        //then & assert
        List<OverviewBook> response=bookService.overviewBooks();
        assertNotNull(response,"Book shouldn't be null!");
        assertEquals("1234567890",response.get(0).getIsbn(),"ISBN should match!");
        assertEquals("Test Book", response.get(0).getTitle(), "Title should match");
        verify(bookRepo,times(1)).findAll();

    }

    @Test
    void modifyBookDetailsSuccess() {
        // given
        RequestBook updateBook = RequestBook.builder()
                .isbn("0987654321")
                .title("Updated Book")
                .edition("2nd")
                .language("Arabic")
                .coverUrl("http://example.com/updated.jpg")
                .summary("Updated summary")
                .publicationYear(2021)
                .build();
        Category updatedCategory = Category.builder().id(2L).name("Non-Fiction").build();

        Publisher updatedPublisher = Publisher.builder().id(2L).name("Nader Fouda").build();

        Author updatedAuthor = Author.builder().id(2L).name("Ahmed Tawfik").build();
        Book updatedBook = Book.builder()
                .id(1L)
                .isbn("0987654321")
                .title("Updated Book")
                .edition("2nd")
                .language("Arabic")
                .coverUrl("http://example.com/updated.jpg")
                .summary("Updated summary")
                .publicationYear(2021)
                .publisher(updatedPublisher)
                .categories(List.of(updatedCategory))
                .authors(List.of(updatedAuthor))
                .build();
        List<Long> categoryIds = List.of(2L);
        List<Long> authorIds = List.of(2L);
        Long publisherId = 2L;

        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepo.findByIsbn("0987654321")).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(categoryIds)).thenReturn(List.of(updatedCategory));
        when(authorRepo.findAllById(authorIds)).thenReturn(List.of(updatedAuthor));
        when(publisherRepo.findById(publisherId)).thenReturn(Optional.of(updatedPublisher));
        when(bookRepo.save(any(Book.class))).thenReturn(updatedBook);

        // then
        ShowBook response = bookService.modifyBookDetails(1L, updateBook, categoryIds, publisherId, authorIds);

        // assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Updated Book", response.getTitle(), "Title should match");
        assertEquals("0987654321", response.getIsbn(), "ISBN should match");
        assertEquals(List.of("Non-Fiction"), response.getCategoriesName(), "Categories should match");
        assertEquals(List.of(2L), response.getAuthorIds(), "Author IDs should match");
        assertEquals(2L, response.getPublisherId(), "Publisher ID should match");
        verify(bookRepo, times(1)).findById(1L);
        verify(bookRepo, times(1)).findByIsbn("0987654321");
        verify(categoryRepo, times(1)).findAllById(categoryIds);
        verify(publisherRepo, times(1)).findById(publisherId);
        verify(authorRepo, times(1)).findAllById(authorIds);
        verify(bookRepo, times(1)).save(any(Book.class));
    }

    @Test
    void modifyBookDetails_throwEntityNotFoundException_whenBookNotExist(){
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.empty());
        //then
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->bookService.modifyBookDetails(1L,requestBook,null,null,null),
                "should throw EntityNotFoundException because book not exist");
        assertEquals("Can't find book with this id :1", notFoundException.getMessage());
        verify(bookRepo,times(1)).findById(1L);
        verify(bookRepo, never()).findByIsbn(anyString());
        verify(categoryRepo, never()).findAllById(anyList());
        verify(publisherRepo, never()).findById(anyLong());
        verify(authorRepo, never()).findAllById(anyList());
        verify(bookRepo, never()).save(any(Book.class));
    }

    @Test
    void modifyBookDetails_throwEntityFoundException_whenUpdateBookExist(){
        //given
        RequestBook updateBook = RequestBook.builder().isbn("0987654321").build();
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepo.findByIsbn("0987654321")).thenReturn(Optional.of(new Book()));
        //then
        EntityFoundException exception=assertThrows(EntityFoundException.class,
                ()->bookService.modifyBookDetails(1L,updateBook,null,null,null),
                "should throw EntityFoundException because new ISBN exist before");
        assertEquals("ISBN is already exist :0987654321", exception.getMessage());
        verify(bookRepo,times(1)).findById(1L);
        verify(bookRepo, times(1)).findByIsbn("0987654321");
        verify(categoryRepo, never()).findAllById(anyList());
        verify(publisherRepo, never()).findById(anyLong());
        verify(authorRepo, never()).findAllById(anyList());
        verify(bookRepo, never()).save(any(Book.class));
    }

    @Test
    void modifyBookDetails_throwEntityNotFoundException_whenCategoriesNotFound(){
        //given
        List<Long> categoryIds=List.of(1L,2L);
        RequestBook updateBook = RequestBook.builder().isbn("0987654321").build();
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepo.findByIsbn("0987654321")).thenReturn(Optional.empty());
        when(categoryRepo.findAllById(categoryIds)).thenReturn(List.of(category));
        //then
        EntityNotFoundException exception=assertThrows(EntityNotFoundException.class,
                ()->bookService.modifyBookDetails(1L,updateBook,categoryIds,null,null),
                "should throw EntityNotFoundException because missed one or more category!");
        assertEquals("One or more categories not found", exception.getMessage());
        verify(bookRepo,times(1)).findById(1L);
        verify(bookRepo, times(1)).findByIsbn("0987654321");
        verify(categoryRepo, times(1)).findAllById(categoryIds);
        verify(publisherRepo, never()).findById(anyLong());
        verify(authorRepo, never()).findAllById(anyList());
        verify(bookRepo, never()).save(any(Book.class));
    }


    @Test
    void removeBookSuccess() {
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingService.isBookCurrentlyBorrowed(1L)).thenReturn(false);
        doNothing().when(bookRepo).deleteById(1L);
        //then & assert
        bookService.removeBook(1L);
        verify(bookRepo,times(1)).findById(1L);
        verify(borrowingService,times(1)).isBookCurrentlyBorrowed(1L);
        verify(bookRepo,times(1)).deleteById(1L);

    }

    @Test
    void removeBook_throwEntityNotFound_whenBookToDeleteNotExist() {
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.empty());
        //then & assert
        EntityNotFoundException exception=assertThrows(EntityNotFoundException.class,
                ()->bookService.removeBook(1L),
                "should throw EntityNotFoundException because Book not found!");
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepo,times(1)).findById(1L);
        verify(borrowingService,never()).isBookCurrentlyBorrowed(anyLong());
        verify(bookRepo,never()).deleteById(anyLong());

    }

    @Test
    void removeBook_throwBookNotAvailableNowException_whenBookIsBorrowed() {
        //when
        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingService.isBookCurrentlyBorrowed(1L)).thenReturn(true);
        //then & assert
        BookNotAvailableNowException exception=assertThrows(BookNotAvailableNowException.class,
                ()->bookService.removeBook(1L),
                "should throw BookNotAvailableNowException because Book now borrowed!");
        assertEquals("Can't delete this book 'Test Book' because it borrowed now !!", exception.getMessage());
        verify(bookRepo,times(1)).findById(1L);
        verify(borrowingService,times(1)).isBookCurrentlyBorrowed(1L);
        verify(bookRepo,never()).deleteById(anyLong());

    }

    @Test
    void isBookAvailableOkay() {
        //when
        when(bookRepo.existsById(1L)).thenReturn(true);
        when(borrowingService.isBookCurrentlyBorrowed(1L)).thenReturn(false);
        //then & assert
        boolean available=bookService.isBookAvailable(1L);

        assertTrue(available);
        verify(bookRepo,times(1)).existsById(1L);
        verify(borrowingService,times(1)).isBookCurrentlyBorrowed(1L);
    }
    @Test
    void isBookAvailable_throwsEntityNotFoundException_whenBookNotFound() {
        // Arrange
        when(bookRepo.existsById(1L)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.isBookAvailable(1L),
                "Should throw EntityNotFoundException because book not found!");
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepo, times(1)).existsById(1L);
        verify(borrowingService, never()).isBookCurrentlyBorrowed(anyLong());
    }

    @Test
    void isBookAvailable_success_notAvailable() {
        // Arrange
        when(bookRepo.existsById(1L)).thenReturn(true);
        when(borrowingService.isBookCurrentlyBorrowed(1L)).thenReturn(true);

        // Act
        boolean isAvailable = bookService.isBookAvailable(1L);

        // Assert
        assertFalse(isAvailable, "Book should not be available");
        verify(bookRepo, times(1)).existsById(1L);
        verify(borrowingService, times(1)).isBookCurrentlyBorrowed(1L);
    }
}