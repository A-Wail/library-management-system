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
import com.task.library_managment_system.reposatory.AuthorRepo;
import com.task.library_managment_system.reposatory.BookRepo;
import com.task.library_managment_system.reposatory.CategoryRepo;
import com.task.library_managment_system.reposatory.PublisherRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepo bookRepo;
    private final AuthorRepo authorRepo;
    private final CategoryRepo categoryRepo;
    private final PublisherRepo publisherRepo;
    private final BorrowingService borrowingService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ShowBook createBook(RequestBook requestBook, List<Long> categoryIds,
                               Long publisherId, List<Long> authorIds) {
        if(bookRepo.findByIsbn(requestBook.getIsbn()).isPresent())
            throw new EntityFoundException("This book is already exist with isbn: "+requestBook.getIsbn());

        List<Category> categories=categoryRepo.findAllById(categoryIds);
        if (categoryIds.size() !=categories.size()){
            throw new EntityNotFoundException("One or more categories not found");
        }
        log.info("Categories step done");

        Publisher publisher=publisherRepo.findById(publisherId)
                    .orElseThrow(()->new EntityNotFoundException("Publisher Not found with id:"+publisherId));
        log.info("Publisher step done");

        List<Author> authors= authorRepo.findAllById(authorIds);

        if (authors.size() != authorIds.size()){
            throw new EntityNotFoundException("One or more authors not found");
        }
        log.info("Authors step done");
        log.info("Creating new book with title: {}", requestBook.getTitle());
        Book book= Book.builder()
                .isbn(requestBook.getIsbn())
                .edition(requestBook.getEdition())
                .title(requestBook.getTitle())
                .language(requestBook.getLanguage())
                .coverUrl(requestBook.getCoverUrl())
                .publisher(publisher)
                .summary(requestBook.getSummary())
                .publicationYear(requestBook.getPublicationYear())
                .categories(categories)
                .authors(authors)
                .build();

        Book savedBook = bookRepo.save(book);
        log.info("Book saved successfully with ID: {}", savedBook.getId());
        return convertToShowBook(savedBook);
    }

    @Override
    @Transactional(readOnly = true)  // Performance optimization
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public ShowBook searchBookByIsbn(String isbn) {
        Book book= bookRepo.findByIsbn(isbn).
                orElseThrow(()->new EntityNotFoundException("Book not found with isbn"+isbn));

        return convertToShowBook(book);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public List<OverviewBook> overviewBooks() {
        return bookRepo.findAll().stream()
                .map(book -> OverviewBook.builder()
                        .isbn(book.getIsbn())
                        .title(book.getTitle())
                        .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ShowBook modifyBookDetails(Long bookId, RequestBook updateBook, List<Long> categoryIds,
                                    Long publisherId, List<Long> authorIds) {

        log.info("Checking if book with id '{}' exist or not",bookId);
        Book bookToUpdate=bookRepo.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("Can't find book with this id :"+bookId));

        log.info("Checking if book isbn '{}' unique or used ",updateBook.getIsbn());
        //check that isbn is unique
        if (!bookToUpdate.getIsbn().equals(updateBook.getIsbn())&&
            bookRepo.findByIsbn(updateBook.getIsbn()).isPresent()){
            log.warn("ISBN is already used and not unique..!");
            throw new EntityFoundException("ISBN is already exist :"+updateBook.getIsbn());
        }

        if (updateBook.getIsbn() != null) bookToUpdate.setIsbn(updateBook.getIsbn());
        if (updateBook.getTitle() != null) bookToUpdate.setTitle(updateBook.getTitle());
        if (updateBook.getPublicationYear() != null) bookToUpdate.setPublicationYear(updateBook.getPublicationYear());
        if (updateBook.getSummary() != null) bookToUpdate.setSummary(updateBook.getSummary());
        if (updateBook.getEdition() != null) bookToUpdate.setEdition(updateBook.getEdition());
        if (updateBook.getLanguage() != null) bookToUpdate.setLanguage(updateBook.getLanguage());
        if (updateBook.getCoverUrl() != null) bookToUpdate.setCoverUrl(updateBook.getCoverUrl());

        log.info("Check if book change parent category or not ");
        if (categoryIds != null){
            List<Category> categories=categoryRepo.findAllById(categoryIds);
            if (categories.size() != categoryIds.size()) {
                log.warn("Missing one or more category in hierarchy of book ..!");
                throw new EntityNotFoundException("One or more categories not found");
            }
            bookToUpdate.setCategories(categories);
            log.info("Categories updated successfully .");
        }

        log.info("Check if there is an update for the authors...");
        if (authorIds != null){
            List<Author> authors=authorRepo.findAllById(authorIds);
            if (authors.size() != authorIds.size()){
                log.warn("Missing one or more authors of book ..!");
                throw new EntityNotFoundException("One or more authors not found");}
            bookToUpdate.setAuthors(authors);
            log.info("authors updated successfully .");
        }

        log.info("Check if there is an update for the publishers...");
        if (publisherId != null){
            Publisher publisher=publisherRepo.findById(publisherId)
                    .orElseThrow(()->new EntityNotFoundException("There is not publisher with id: "+publisherId));
            bookToUpdate.setPublisher(publisher);
            log.info("publisher updated successfully .");
        }

        bookRepo.save(bookToUpdate);
        log.info("Book updated successfully.");

        return convertToShowBook(bookToUpdate);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeBook(Long bookId) {
        log.info("Start to delete book with id: {}", bookId);

        //Check if the book exists
        Book bookToDelete = bookRepo.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
        //Check if book is borrowed or available
        boolean isBorrowed = borrowingService.isBookCurrentlyBorrowed(bookId);

        if (isBorrowed){
            String errorMessage=String.format("Can't delete this book %s because it borrowed now !!"
                    ,bookToDelete.getTitle());
            log.warn(errorMessage);
            throw new BookNotAvailableNowException(errorMessage);
        }
        bookRepo.deleteById(bookId);
        log.info("Book with id:{} successfully deleted.",bookId);
    }

    @Override
    public boolean isBookAvailable(Long bookId) {
        log.info("Checking available for book with id:"+bookId);
        if(!bookRepo.existsById(bookId)) {
            log.warn("Not found book with id:{}",bookId);
            throw new EntityNotFoundException("Book not found with id: " + bookId);
        }
        boolean isBorrowed = borrowingService.isBookCurrentlyBorrowed(bookId);
        boolean isAvailable=!isBorrowed;
        log.debug("Book id: {} - borrowed: {}, available: {}", bookId, isBorrowed, isAvailable);
        return isAvailable;
    }
    private ShowBook convertToShowBook(Book book) {
        return ShowBook.builder()
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .summary(book.getSummary())
                .coverUrl(book.getCoverUrl())
                .publicationYear(book.getPublicationYear())
                .edition(book.getEdition())
                .language(book.getLanguage())
                .publisherId(book.getPublisher().getId())
                .authorIds(book.getAuthors().stream().map(Author::getId).collect(Collectors.toList()))
                .categoriesName(book.getCategories().stream().map(Category::getName).collect(Collectors.toList()))
                .build();
    }
}
