package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.borrow.BorrowingResponse;
import com.task.library_managment_system.dto.borrow.ReturnedResponse;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.book.BookNotAvailableNowException;
import com.task.library_managment_system.exception.transaction.BookAlreadyReturnedException;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.BorrowingTransaction;
import com.task.library_managment_system.models.Member;
import com.task.library_managment_system.repository.BookRepo;
import com.task.library_managment_system.repository.BorrowingTransRepo;
import com.task.library_managment_system.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowingServiceImpl implements BorrowingService{

    private final BorrowingTransRepo borrowingTransRepo;
    private final MemberRepo memberRepo;
    private final BookRepo bookRepo;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public BorrowingResponse borrowBook(Long memberId, Long bookId) {

        log.info("Processing borrow request - Member ID: {}, Book ID: {}", memberId, bookId);
        log.info("Check if borrower exist or not ... ");
        Member borrower = memberRepo.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("No member with this id:"+memberId));

        log.info("Check if borrowedBook exist or not ... ");
        Book borrowedBook =bookRepo.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("No book with this id:"+bookId));

        log.info("Check if book available or not ... ");
        if (isBookCurrentlyBorrowed(bookId)){
            String errorMessage=String.format("Can't borrow this book %s because it borrowed now !!"
                    ,borrowedBook.getTitle());
            log.warn(errorMessage);
            throw new BookNotAvailableNowException(errorMessage);
        }

        BorrowingTransaction transaction=BorrowingTransaction.builder()
                .book(borrowedBook)
                .member(borrower)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(BorrowingTransaction.transactionState.BORROWED)
                .build();

        log.info("Borrowing transaction is processing now ...");

        BorrowingTransaction savedTransaction= borrowingTransRepo.save(transaction);
        log.info("Borrowing transaction done.");

        return BorrowingResponse.builder()
                .transactionId(savedTransaction.getId())
                .bookTitle(borrowedBook.getTitle())
                .memberName(borrower.getName())
                .borrowDate(savedTransaction.getBorrowDate())
                .dueDate(savedTransaction.getDueDate())
                .message("Book borrowed successfully. Due date: " + savedTransaction.getDueDate())
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public ReturnedResponse getBorrowingById(Long transactionId) {

        log.info("Check if transaction exist or not ... ");
        BorrowingTransaction transaction=borrowingTransRepo.findById(transactionId)
                .orElseThrow(()->new EntityNotFoundException("can't find transaction with id:"+transactionId));

        log.info("Check if transaction already returned ... ");
        if (transaction.getStatus() != BorrowingTransaction.transactionState.BORROWED){
            log.warn("Book already returned!");
            throw new BookAlreadyReturnedException("Book already returned: "+transactionId);
        }

        transaction.setReturnDate(LocalDate.now());

        log.info("Check if book returned after or before it's dueDate ... ");
        if (transaction.getDueDate().isBefore(transaction.getReturnDate())){
            transaction.setStatus(BorrowingTransaction.transactionState.OVERDUE);
            log.warn("Overdue return detected. Transaction ID: {}", transactionId);
        }else {
            transaction.setStatus(BorrowingTransaction.transactionState.RETURNED);
        }

        borrowingTransRepo.save(transaction);
        log.info("Book returned successfully.");
        return ReturnedResponse.builder()
                .transactionId(transaction.getId())
                .bookTitle(transaction.getBook().getTitle())
                .memberName(transaction.getMember().getName())
                .borrowDate(transaction.getBorrowDate())
                .returnDate(transaction.getReturnDate())
                .dueDate(transaction.getDueDate())
                .status(transaction.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookCurrentlyBorrowed(Long bookId) {
            log.debug("Check if current book id {} is currently borrowed",bookId);
            List<BorrowingTransaction> activeTransactions=findActiveTransactionsByBookId(bookId);
        return !activeTransactions.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowingTransaction> findActiveTransactionsByBookId(Long bookId) {
        log.debug("Finding active transactions for book id:"+bookId);
        List<BorrowingTransaction> allTransactionsForBook =borrowingTransRepo.findByBookId(bookId);

        return allTransactionsForBook.stream()
                .filter(transaction -> transaction.getReturnDate()==null)
                .toList();
    }


}
