package com.task.library_managment_system.controller;

import com.task.library_managment_system.dto.book.CreateBookRequest;
import com.task.library_managment_system.dto.book.OverviewBook;
import com.task.library_managment_system.dto.book.RequestBook;
import com.task.library_managment_system.dto.book.ShowBook;
import com.task.library_managment_system.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping()
    public ResponseEntity<ShowBook> newBook(@Valid @RequestBody CreateBookRequest request){

        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request.getBookDetails(),request.getCategoryIds(),
                                                        request.getPublisherId(), request.getAuthorIds()));
    }

    @GetMapping()
    public ResponseEntity<List<OverviewBook>> showAllBooks(){
        return ResponseEntity.ok(bookService.overviewBooks());
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<ShowBook> searchBookById(@PathVariable String isbn){
        return ResponseEntity.ok(bookService.searchBookByIsbn(isbn));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ShowBook> updateBook(@PathVariable Long bookId,
                                               @Valid @RequestBody  CreateBookRequest request){

        return ResponseEntity.ok(bookService.modifyBookDetails
                (bookId,request.getBookDetails(),request.getCategoryIds(),
                        request.getPublisherId(), request.getAuthorIds()));

    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@RequestParam Long bookId){
        bookService.removeBook(bookId);
        return ResponseEntity.noContent().build();
    }

}
