package com.task.library_managment_system.controller;

import com.task.library_managment_system.dto.category.CategoryRequestBody;
import com.task.library_managment_system.dto.category.CategoryResponse;
import com.task.library_managment_system.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public ResponseEntity<CategoryResponse> insert(@Valid @RequestBody CategoryRequestBody request){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request.getCategory(), request.getParentId()));
    }

    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> showAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> searchById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.viewCategoryById(id));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateBook(@PathVariable Long categoryId,
                                               @Valid @RequestBody CategoryRequestBody request){

        return ResponseEntity.ok(categoryService
                .updateCategory(categoryId,request.getCategory(), request.getParentId()));

    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@RequestParam Long bookId){
        categoryService.deleteCategory(bookId);
        return ResponseEntity.noContent().build();
    }

}
