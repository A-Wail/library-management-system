package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.borrow.BorrowingResponse;
import com.task.library_managment_system.dto.borrow.ReturnedResponse;
import com.task.library_managment_system.models.BorrowingTransaction;

import java.util.List;

public interface BorrowingService {
     BorrowingResponse borrowBook(Long memberId, Long bookId);
     ReturnedResponse getBorrowingById(Long transactionId);
     boolean isBookCurrentlyBorrowed(Long bookId);
     List<BorrowingTransaction> findActiveTransactionsByBookId(Long bookId);
}
