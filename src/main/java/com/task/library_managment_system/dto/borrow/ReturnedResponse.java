package com.task.library_managment_system.dto.borrow;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReturnedResponse {
    private Long transactionId;
    private String bookTitle;
    private String memberName;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private String status ;
}
