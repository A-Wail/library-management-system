package com.task.library_managment_system.controllerAdvice;


import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.ErrorResponse;
import com.task.library_managment_system.exception.author.AuthorAssociatedBooksException;
import com.task.library_managment_system.exception.book.BookNotAvailableNowException;
import com.task.library_managment_system.exception.category.CategoryAssociatedBooksException;
import com.task.library_managment_system.exception.category.CategoryContainsChildrenException;
import com.task.library_managment_system.exception.category.CategoryHierarchyCycleException;
import com.task.library_managment_system.exception.member.MemberHasTransactionException;
import com.task.library_managment_system.exception.publisher.PublisherAssociatedBooksException;
import com.task.library_managment_system.exception.transaction.BookAlreadyReturnedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    public ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message){
        ErrorResponse error=new ErrorResponse(status.value(),message,System.currentTimeMillis());
        return new ResponseEntity<>(error,status);
    }

    @ExceptionHandler(EntityFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityFoundException(EntityFoundException ex){
        return buildErrorResponse(HttpStatus.FOUND,ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityFoundException e){
        return buildErrorResponse(HttpStatus.NOT_FOUND,e.getMessage());}

    @ExceptionHandler(BookNotAvailableNowException.class)
    public ResponseEntity<ErrorResponse> handleBookNotAvailableNowException(BookNotAvailableNowException e){
        return buildErrorResponse(HttpStatus.CONFLICT,e.getMessage());}

    @ExceptionHandler(BookAlreadyReturnedException.class)
    public ResponseEntity<ErrorResponse> handleBookAlreadyReturnedException(BookAlreadyReturnedException ex){
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());}

    @ExceptionHandler(AuthorAssociatedBooksException.class)
    public ResponseEntity<ErrorResponse> handleAuthorAssociatedBooksException(AuthorAssociatedBooksException ex){
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());}

    @ExceptionHandler(CategoryHierarchyCycleException.class)
    public ResponseEntity<ErrorResponse> handleCategoryHierarchyCycleException(CategoryHierarchyCycleException ex){
        return buildErrorResponse(HttpStatus.LOOP_DETECTED,ex.getMessage());}

    @ExceptionHandler(CategoryContainsChildrenException.class)
    public ResponseEntity<ErrorResponse> handleCategoryContainsChildrenException(CategoryContainsChildrenException ex){
        return buildErrorResponse(HttpStatus.LOOP_DETECTED,ex.getMessage());}

    @ExceptionHandler(CategoryAssociatedBooksException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAssociatedBooksException(CategoryAssociatedBooksException ex){
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());}

    @ExceptionHandler(PublisherAssociatedBooksException.class)
    public ResponseEntity<ErrorResponse> handlePublisherAssociatedBooksException(PublisherAssociatedBooksException ex){
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());}

    @ExceptionHandler(MemberHasTransactionException.class)
    public ResponseEntity<ErrorResponse> handleMemberHasTransactionException(MemberHasTransactionException ex){
        return buildErrorResponse(HttpStatus.CONFLICT,ex.getMessage());}

    //Method for handle validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
