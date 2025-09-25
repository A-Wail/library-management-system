package com.task.library_managment_system.exception.transaction;

public class BookAlreadyReturnedException extends RuntimeException {
    public BookAlreadyReturnedException(String s) {
        super(s);
    }
}
