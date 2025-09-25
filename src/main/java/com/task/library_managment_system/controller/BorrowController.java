package com.task.library_managment_system.controller;

import com.task.library_managment_system.dto.borrow.BorrowingResponse;
import com.task.library_managment_system.dto.borrow.ReturnedResponse;
import com.task.library_managment_system.service.BorrowingService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowingService borrowingService;

    @PostMapping("/member/{memberId}/book/{bookId}")
    public ResponseEntity<BorrowingResponse> borrowBook(@PathVariable @Min(1) Long memberId,
                                                        @PathVariable @Min(1) Long bookId){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowingService.borrowBook(memberId,bookId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReturnedResponse> returnBook(@PathVariable Long id){

        return ResponseEntity.ok(borrowingService.getBorrowingById(id));

    }

}
