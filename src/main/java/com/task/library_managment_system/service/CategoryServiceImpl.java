package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.category.CategoryResponse;
import com.task.library_managment_system.dto.category.RequestCategory;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.category.CategoryAssociatedBooksException;
import com.task.library_managment_system.exception.category.CategoryContainsChildrenException;
import com.task.library_managment_system.exception.category.CategoryHierarchyCycleException;
import com.task.library_managment_system.models.Category;
import com.task.library_managment_system.reposatory.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepo categoryRepo;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public CategoryResponse createCategory(RequestCategory request, Long parentId) {
        Category category=categoryRepo.findByName(request.getName())
                .orElseThrow(()->new EntityFoundException("Category you want to add is already exist :"
                                    +request.getName() ));

        if (parentId !=null){
            Category parent=categoryRepo.findById(parentId).
                    orElseThrow(()->new EntityNotFoundException("Parent category not found"));

            category.setParentCategory(parent);
            checkForCycle(category);
        }
        categoryRepo.save(category);
        log.info("Category saved successfully.");
        return convertToCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepo.findAll().stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public CategoryResponse viewCategoryById(Long categoryId) {
        Category category=categoryRepo.findById(categoryId)
                .orElseThrow(()->new EntityNotFoundException("Category not found !!"));
        return convertToCategoryResponse(category);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public CategoryResponse updateCategory(Long categoryId, RequestCategory updatedCategory, Long parentId) {

        Category existCategory=categoryRepo.findById(categoryId)
                .orElseThrow(()->new EntityNotFoundException("Category not found !!"));

        log.info("Check if updated category exist already or not...");
        if (!existCategory.getName().equals(updatedCategory.getName())&&
            categoryRepo.findByName(updatedCategory.getName()).isPresent()){

            log.warn("Category already exist with name {}",updatedCategory.getName());

            throw new EntityFoundException("Updated category already exist before !!");
        }

        if (updatedCategory.getName() != null)  existCategory.setName(updatedCategory.getName());

        if (parentId !=null){
            Category parent=categoryRepo.findById(parentId).
                    orElseThrow(()->new EntityNotFoundException("Parent category not found:"+parentId));

            existCategory.setParentCategory(parent);
            checkForCycle(existCategory);
        }else {
            existCategory.setParentCategory(null);
        }
        categoryRepo.save(existCategory);
        log.info("Category updated successfully.");

        return convertToCategoryResponse(existCategory) ;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long categoryId) {
        Category deletedCategory=categoryRepo.findById(categoryId)
                .orElseThrow(()->new EntityNotFoundException("Category not found !!"));
        log.info("Check if category to delete hase children category or not...");
        if (!deletedCategory.getSubCategories().isEmpty()){
            log.warn("Category '{}' to delete first you want to delete it's children "
                    ,deletedCategory.getName());
            throw new CategoryContainsChildrenException("Can't delete category with children !!");
        }
        log.info("Check if deleted category contains books ...");
        if (deletedCategory.getBooks().isEmpty()){
            log.warn("Category '{}' to delete first you want to delete books that contains !!",
                    deletedCategory.getName());
            throw new CategoryAssociatedBooksException("Can't delete category that has books !");
        }

        categoryRepo.delete(deletedCategory);
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentCategory().getId())
                .build();
    }

    private void checkForCycle(Category category) {
        Category currentCategory=category;
        Set<Long> visited=new HashSet<>();
        //check if our category has cycle or not
        while (currentCategory != null && category.getParentCategory() != null) {

            if (visited.contains(currentCategory.getParentCategory().getId())){
                throw new CategoryHierarchyCycleException("Category hierarchy contains cycle !!");
            }
            visited.add(currentCategory.getId());
            currentCategory=currentCategory.getParentCategory();
        }
    }
}
