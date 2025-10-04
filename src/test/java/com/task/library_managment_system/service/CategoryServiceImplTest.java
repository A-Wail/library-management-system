package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.category.CategoryResponse;
import com.task.library_managment_system.dto.category.RequestCategory;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.category.CategoryAssociatedBooksException;
import com.task.library_managment_system.exception.category.CategoryContainsChildrenException;
import com.task.library_managment_system.exception.category.CategoryHierarchyCycleException;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.Category;
import com.task.library_managment_system.repository.CategoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock private CategoryRepo categoryRepo;
    @InjectMocks private CategoryServiceImpl service;
    private Category category;
    private RequestCategory requestCategory;


    @BeforeEach
    void setUp() {
        category= Category.builder()
                .id(1L)
                .name("Fiction")
                .books(Collections.emptyList())
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .build();

        requestCategory=RequestCategory.builder()
                .name("Fiction")
                .build();


    }

    @Test
    void createCategorySuccess_withoutParent() {

        //When
        when(categoryRepo.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.save(any(Category.class))).thenReturn(category);
        //Then & Assert
        CategoryResponse response=service.createCategory(requestCategory,null);

        assertNotNull(response,"Response should not null!");
        assertEquals("Fiction", response.getName(), "Name should match");
        assertNull(response.getParentId(), "Parent ID should be null");
        verify(categoryRepo, times(1)).findByName("Fiction");
        verify(categoryRepo, times(1)).save(any(Category.class));
    }

    @Test
    void createCategorySuccess_withParent() {
        //Given
        Category parentCategory=Category.builder()
                .id(2L)
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .name("Literature")
                .build();
        Category newCategory= Category.builder()
                .id(1L)
                .name("Fiction")
                .books(Collections.emptyList())
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .build();
        //When
        when(categoryRepo.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepo.save(any(Category.class))).thenReturn(newCategory);
        //Then & Assert
        CategoryResponse response=service.createCategory(requestCategory,2L);

        assertNotNull(response,"Response should not null!");
        assertEquals("Fiction", response.getName(), "Name should match");
        assertEquals(2L,response.getParentId(), "Parent ID should match");
        verify(categoryRepo, times(1)).findByName("Fiction");
        verify(categoryRepo, times(1)).findById(2L);
        verify(categoryRepo, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_throwsEntityFoundException_whenNameExists() {
        // Arrange: Mock repository to return existing category
        when(categoryRepo.findByName("Fiction")).thenReturn(Optional.of(category));

        // Act & Assert: Verify exception
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> service.createCategory(requestCategory, null),
                "Should throw EntityFoundException");
        assertEquals("Category you want to add is already exist: Fiction", exception.getMessage());
        verify(categoryRepo, times(1)).findByName("Fiction");
        verify(categoryRepo, never()).findById(anyLong());
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void createCategory_throwsEntityNotFoundException_whenParentNotFound() {
        // When
        when(categoryRepo.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.findById(2L)).thenReturn(Optional.empty());

        // Then & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.createCategory(requestCategory, 2L),
                "Should throw EntityNotFoundException");
        assertEquals("Parent category not found", exception.getMessage());
        verify(categoryRepo, times(1)).findByName("Fiction");
        verify(categoryRepo, times(1)).findById(2L);
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void createCategory_throwsCategoryHierarchyCycleException_whenCycleException() {
        //Given
        Category parentCategory=Category.builder()
                .id(2L)
                .parentCategory(category)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .name("Literature")
                .build();
        category.setParentCategory(parentCategory);
        // Then
        when(categoryRepo.findByName("Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parentCategory));

        // Act & Assert
        CategoryHierarchyCycleException exception = assertThrows(CategoryHierarchyCycleException.class,
                () -> service.createCategory(requestCategory, 2L),
                "Should throw CategoryHierarchyCycleException");
        assertEquals("Category hierarchy contains cycle !!", exception.getMessage());
        verify(categoryRepo, times(1)).findByName("Fiction");
        verify(categoryRepo, times(1)).findById(2L);
        verify(categoryRepo, never()).save(any(Category.class));
    }


    @Test
    void getAllCategoriesSuccess() {
        // Given
        Category category2 = Category.builder()
                .id(2L)
                .name("Non-Fiction")
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        when(categoryRepo.findAll()).thenReturn(Arrays.asList(category, category2));

        // Then
        List<CategoryResponse> responses = service.getAllCategories();

        // Assert: Verify response
        assertEquals(2, responses.size(), "Should return two categories");
        assertEquals("Fiction", responses.get(0).getName(), "First category name should match");
        assertEquals("Non-Fiction", responses.get(1).getName(), "Second category name should match");
        verify(categoryRepo, times(1)).findAll();
    }


    @Test
    void viewCategoryByIdSuccess() {
        // Arrange: Mock repository to return category
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

        // Act: Call viewCategoryById
        CategoryResponse response = service.viewCategoryById(1L);

        // Assert: Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals(1L, response.getId(), "ID should match");
        assertEquals("Fiction", response.getName(), "Name should match");
        verify(categoryRepo, times(1)).findById(1L);
    }

    @Test
    void viewCategoryById_throwsEntityNotFoundException() {
        // Arrange: Mock repository to return no category
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify exception
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.viewCategoryById(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Category not found !!", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
    }


    @Test
    void updateCategorySuccess_WithoutParent() {
        // Arrange: Mock repository for updating category
        RequestCategory updatedRequest = RequestCategory.builder()
                .name("Updated Fiction")
                .build();
        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Fiction")
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepo.findByName("Updated Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.save(any(Category.class))).thenReturn(updatedCategory);

        // Act: Call updateCategory without parent
        CategoryResponse response = service.updateCategory(1L, updatedRequest, null);

        // Assert: Verify response
        assertNotNull(response, "Response should not be null");
        assertEquals("Updated Fiction", response.getName(), "Name should match");
        assertNull(response.getParentId(), "Parent ID should be null");
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, times(1)).findByName("Updated Fiction");
        verify(categoryRepo, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategorySuccess_WithParent() {
        // Given
        RequestCategory updatedRequest = RequestCategory.builder()
                .name("Updated Fiction")
                .build();
        Category parent = Category.builder()
                .id(2L)
                .name("Literature")
                .parentCategory(null)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Fiction")
                .parentCategory(parent)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepo.findByName("Updated Fiction")).thenReturn(Optional.empty());
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parent));
        when(categoryRepo.save(any(Category.class))).thenReturn(updatedCategory);

        // Then
        CategoryResponse response = service.updateCategory(1L, updatedRequest, 2L);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Updated Fiction", response.getName(), "Name should match");
        assertEquals(2L, response.getParentId(), "Parent ID should match");
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, times(1)).findByName("Updated Fiction");
        verify(categoryRepo, times(1)).findById(2L);
        verify(categoryRepo, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsEntityNotFoundException_whenCategoryNotFound() {
        // Given
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

        // Then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.updateCategory(1L, requestCategory, null),
                "Should throw EntityNotFoundException");
        assertEquals("Category not found !!", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, never()).findByName(anyString());
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsEntityFoundException_whenNameExists() {
        // Given
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepo.findByName("Updated Fiction")).thenReturn(Optional.of(new Category()));

        // Then & assert
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> service.updateCategory(1L, RequestCategory.builder().name("Updated Fiction").build(), null),
                "Should throw exception!");
        assertEquals("Updated category already exist before !!", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, times(1)).findByName("Updated Fiction");
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsEntityNotFoundException_whenParentNotFound() {
        // Given
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepo.findById(2L)).thenReturn(Optional.empty());

        // Then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.updateCategory(1L, requestCategory, 2L),
                "Should throw EntityNotFoundException");
        assertEquals("Parent category not found:2", exception.getMessage());
        verify(categoryRepo, times(2)).findById(anyLong());
        verify(categoryRepo,never()).findByName(anyString());
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsCategoryHierarchyCycleException() {
        // Given
        Category parent = Category.builder()
                .id(2L)
                .name("Literature")
                .parentCategory(category)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        // Then & assert
        category.setParentCategory(parent);
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(parent));

        CategoryHierarchyCycleException exception = assertThrows(CategoryHierarchyCycleException.class,
                () -> service.updateCategory(1L, requestCategory, 2L),
                "Should throw CategoryHierarchyCycleException");
        assertEquals("Category hierarchy contains cycle !!", exception.getMessage());
        verify(categoryRepo, times(2)).findById(anyLong());
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void deleteCategorySuccess() {
        // When
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepo).delete(category);

        // Then
        service.deleteCategory(1L);

        // Assert: Verify interactions
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, times(1)).delete(category);
    }

    @Test
    void deleteCategory_throwsEntityNotFoundException() {
        // When
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

        // Then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> service.deleteCategory(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Category not found !!", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_throwsCategoryContainsChildrenException() {
        // Given
        Category subcategory = Category.builder()
                .id(2L)
                .name("Subcategory")
                .parentCategory(category)
                .subCategories(Collections.emptyList())
                .books(Collections.emptyList())
                .build();
        category.setSubCategories(List.of(subcategory));
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

        // Then & Assert
        CategoryContainsChildrenException exception = assertThrows(CategoryContainsChildrenException.class,
                () -> service.deleteCategory(1L),
                "Should throw CategoryContainsChildrenException");
        assertEquals("Can't delete category with children !!", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategoryThrowsCategoryAssociatedBooksException() {
        // Given
        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .build();
        category.setBooks(List.of(book));
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

        //Then & Assert
        CategoryAssociatedBooksException exception = assertThrows(CategoryAssociatedBooksException.class,
                () -> service.deleteCategory(1L),
                "Should throw CategoryAssociatedBooksException");
        assertEquals("Can't delete category that has books !", exception.getMessage());
        verify(categoryRepo, times(1)).findById(1L);
        verify(categoryRepo, never()).delete(any(Category.class));
    }
}