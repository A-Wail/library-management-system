package com.task.library_managment_system.exception.category;

public class CategoryContainsChildrenException extends RuntimeException {
    public CategoryContainsChildrenException(String msg) {
        super(msg);
    }
}
