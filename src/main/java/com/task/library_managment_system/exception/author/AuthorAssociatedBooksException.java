package com.task.library_managment_system.exception.author;

public class AuthorAssociatedBooksException extends RuntimeException {
    public AuthorAssociatedBooksException(String errorMessage) {
        super(errorMessage);
    }
}
