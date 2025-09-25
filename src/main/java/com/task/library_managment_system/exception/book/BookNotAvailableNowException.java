package com.task.library_managment_system.exception.book;

public class BookNotAvailableNowException extends RuntimeException {
    public BookNotAvailableNowException(String message) {
        super(message);
    }
}
