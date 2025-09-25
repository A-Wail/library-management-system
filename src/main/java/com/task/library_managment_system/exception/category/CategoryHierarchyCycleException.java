package com.task.library_managment_system.exception.category;

public class CategoryHierarchyCycleException extends RuntimeException {
    public CategoryHierarchyCycleException(String s) {
        super(s);
    }
}
