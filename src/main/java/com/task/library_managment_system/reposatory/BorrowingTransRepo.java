package com.task.library_managment_system.reposatory;

import com.task.library_managment_system.models.Author;
import com.task.library_managment_system.models.BorrowingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingTransRepo extends JpaRepository<BorrowingTransaction,Long> {
    List<BorrowingTransaction> findByBookId(Long bookId);
}
