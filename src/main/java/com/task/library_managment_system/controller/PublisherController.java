package com.task.library_managment_system.controller;


import com.task.library_managment_system.dto.publisher.PublisherRequest;
import com.task.library_managment_system.dto.publisher.PublisherResponse;
import com.task.library_managment_system.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/publisher")
@RequiredArgsConstructor
public class PublisherController {
    private final PublisherService publisherService;

    @PostMapping()
    public ResponseEntity<PublisherResponse> insert(@Valid @RequestBody PublisherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherService.createPublisher(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherResponse> update(@PathVariable Long id,@Valid @RequestBody PublisherRequest request) {
        return ResponseEntity.ok(publisherService.updatePublisher(id,request));
    }

    @GetMapping()
    public ResponseEntity<List<PublisherResponse>> getAllPublisher(){
        return ResponseEntity.ok(publisherService.viewAllPublisher());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> getById(@PathVariable Long id){
     return ResponseEntity.ok(publisherService.viewPublisherById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Long id){
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }


}


