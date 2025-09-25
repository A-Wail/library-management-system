package com.task.library_managment_system.controller;


import com.task.library_managment_system.dto.author.AuthorResponse;
import com.task.library_managment_system.dto.author.RequestAuthor;
import com.task.library_managment_system.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/author")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping()
    public ResponseEntity<AuthorResponse> insertAuthor(@Valid @RequestBody RequestAuthor request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.addAuthor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> update(@PathVariable Long id, @Valid@RequestBody RequestAuthor requestAuthor) {
        return ResponseEntity.ok(authorService.updateAuthor(id, requestAuthor));
    }

    @GetMapping()
    public ResponseEntity<List<AuthorResponse>> getAllAuthors(){
        return ResponseEntity.ok(authorService.viewAuthors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id){
     return ResponseEntity.ok(authorService.viewAuthorById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Long id){
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }


}


