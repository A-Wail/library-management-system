package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.author.AuthorResponse;
import com.task.library_managment_system.dto.author.RequestAuthor;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.author.AuthorAssociatedBooksException;
import com.task.library_managment_system.models.Author;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.repository.AuthorRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock private AuthorRepo repo;
    @InjectMocks private AuthorServiceImpl authorService;
    private Author author;
    private RequestAuthor requestAuthor;

    @BeforeEach
    void setUp() {
        author=Author.builder()
                        .name("Ahmed Khaled")
                        .id(1L)
                        .bio("He is a writer and was born in cairo.")
                        .books(Collections.emptyList())
                        .build();
        requestAuthor = RequestAuthor.builder()
                        .name("Ahmed Khaled")
                        .biography("He is a writer and was born in cairo.")
                        .build();
    }

    @Test
    @DisplayName("Check that author successfully insert.")
    void addAuthorSuccessfully() {
        //when
        when(repo.findByName("Ahmed Khaled")).thenReturn(Optional.empty());
        when(repo.save(any(Author.class))).thenReturn(author);
        //then
        AuthorResponse response = authorService.addAuthor(requestAuthor);
        //assert
        assertNotNull(response,"Response can't be null");
        assertEquals(1L,response.getId(),"Id should be match");
        assertEquals("Ahmed Khaled",response.getName(),"Name should be 'Ahmed Khaled'");
        verify(repo,times(1)).findByName("Ahmed Khaled");
        verify(repo,times(1)).save(any(Author.class));
    }

    @Test
    @DisplayName("Check that author throw exception when it founded before.")
    void addAuthorThrowEntityFoundExceptionWhenAuthorExists() {
        //when
        when(repo.findByName("Ahmed Khaled")).thenReturn(Optional.of(author));
        //then
        EntityFoundException exception=assertThrows(EntityFoundException.class,
                ()->authorService.addAuthor(requestAuthor),
                "Should throw entity exception!");
        //assert
        assertEquals("Author already exist !!",exception.getMessage(),"should return exception message!");
        verify(repo,times(1)).findByName("Ahmed Khaled");
        verify(repo,never()).save(any(Author.class));
    }

    @Test
    @DisplayName("Check that author updated.")
    void updateAuthorSuccess() {
        //given
        RequestAuthor updateAuthor=RequestAuthor.builder()
                .name("Ahmed Tawfik")
                .biography("He is a doctor and writer was born on Tanta.")
                .build();
        //when
        when(repo.findById(1L)).thenReturn(Optional.of(author));
        when(repo.findByName(updateAuthor.getName())).thenReturn(Optional.empty());
        AuthorResponse response=authorService.updateAuthor(1L,updateAuthor);
        //assert
        assertNotNull(response,"Response shouldn't be null!");
        assertEquals("Ahmed Tawfik", response.getName(), "Name should match");
        assertEquals("He is a doctor and writer was born on Tanta.", response.getBiography(), "Biography should match");
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).findByName("Ahmed Tawfik");
        verify(repo, times(1)).save(any(Author.class));
    }

    @Test
    @DisplayName("Check that author throw exception when not founded.")
    void updateAuthorThrowEntityNotFoundExceptionWhenAuthorNotExist(){

        //when
        when(repo.findById(1L)).thenReturn(Optional.empty());
        //then
        EntityNotFoundException notFoundException=assertThrows(EntityNotFoundException.class,
                ()->authorService.updateAuthor(1L,requestAuthor),
                "should throw EntityNotFoundException");
        //assert
        assertEquals("Author not found!",notFoundException.getMessage());
        verify(repo,times(1)).findById(1L);
        verify(repo,never()).findByName("Ahmed Tawfik");
        verify(repo,never()).save(any(Author.class));
    }

    @Test
    @DisplayName("Check that author throw exception when update name exist.")
    void updateAuthorThrowEntityFoundExceptionWhenNameExist(){
        //given
        RequestAuthor updatedAuthor= RequestAuthor.builder()
                .name("Jon Doe")
                .biography("Updated biography")
                .build();
        Author existingAuthor= Author.builder()
                .id(2L)
                .name("Jon Doe")
                .bio("Exist biography")
                .books(Collections.emptyList())
                .build();
        //when
        when(repo.findById(1L)).thenReturn(Optional.of(author));
        when(repo.findByName("Jon Doe")).thenReturn(Optional.of(existingAuthor));
        //then
        EntityFoundException foundException=assertThrows(EntityFoundException.class,
                ()->authorService.updateAuthor(1L,updatedAuthor),
                "should throw EntityFoundException");
        //assert
        assertEquals("Author name already existed",foundException.getMessage());
        verify(repo,times(1)).findById(1L);
        verify(repo,times(1)).findByName("Jon Doe");
        verify(repo,never()).save(any(Author.class));
    }

    @Test
    @DisplayName("Check that all author return success.")
    void viewAuthorsSuccess() {
        //given
        Author author2=Author.builder()
                .id(2L)
                .name("Jon Doe")
                .bio("Writer biography")
                .books(Collections.emptyList())
                .build();
        //when
        when(repo.findAll()).thenReturn(Arrays.asList(author,author2));
        //then && assert
        List<AuthorResponse> responses=authorService.viewAuthors();
        assertNotNull(responses);
        assertEquals(2,responses.size(),"Should return two authors");
        assertEquals("Ahmed Khaled", responses.get(0).getName(), "First author name should match");
        assertEquals("Jon Doe", responses.get(1).getName(), "Second author name should match");
        verify(repo, times(1)).findAll();
    }

    @Test
    @DisplayName("Check that when give id return success author.")
    void viewAuthorByIdSuccess() {
        // when
        when(repo.findById(1L)).thenReturn(Optional.of(author));

        // then
        AuthorResponse response = authorService.viewAuthorById(1L);
        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(1L, response.getId(), "ID should match");
        assertEquals("Ahmed Khaled", response.getName(), "Name should match");
        verify(repo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check that when give not exist id throw exception.")
    void viewAuthorByIdThrowsEntityNotFoundExceptionWhenAuthorNotFound() {
        // given
        when(repo.findById(1L)).thenReturn(Optional.empty());

        // then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorService.viewAuthorById(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Author not found!", exception.getMessage());
        verify(repo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check that when give id of author delete it success.")
    void deleteAuthorSuccess() {
        // when
        when(repo.findById(1L)).thenReturn(Optional.of(author));
        doNothing().when(repo).delete(author);
        // then
        authorService.deleteAuthor(1L);
        // Assert
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).delete(author);
    }

    @Test
    @DisplayName("Check that when give no exist author throw exception.")
    void deleteAuthorThrowExceptionWhenAuthorNotExist() {
        // when
        when(repo.findById(1L)).thenReturn(Optional.empty());
        // then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorService.deleteAuthor(1L),
                "Should throw EntityNotFoundException");
        // Assert
        assertEquals("Author Not found id: 1", exception.getMessage());
        verify(repo, times(1)).findById(1L);
        verify(repo,never()).delete(author);
    }

    @Test
    @DisplayName("Check that when give no exist author throw exception.")
    void deleteAuthorThrowExceptionWhenAuthorAssociatedBooks() {
        //given
        Author authorWithBook=Author.builder()
                .id(2L)
                .name("Jon Doe")
                .bio("Exist biography")
                .books(List.of(new Book()))
                .build();
        // when
        when(repo.findById(2L)).thenReturn(Optional.of(authorWithBook));
        // then
        AuthorAssociatedBooksException exception = assertThrows(AuthorAssociatedBooksException.class,
                () -> authorService.deleteAuthor(2L),
                "Should throw AuthorAssociatedBooksException");
        // Assert
        assertTrue(exception.getMessage()
                .contains("author 'Jon Doe' with (id:2) has one or more books associated with them"));
        verify(repo, times(1)).findById(2L);
        verify(repo,never()).delete(author);
    }

}