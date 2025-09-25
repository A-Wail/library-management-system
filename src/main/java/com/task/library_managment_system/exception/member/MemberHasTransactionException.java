package com.task.library_managment_system.exception.member;

public class MemberHasTransactionException extends RuntimeException {
    public MemberHasTransactionException(String errorMessage) {
        super(errorMessage);
    }
}
