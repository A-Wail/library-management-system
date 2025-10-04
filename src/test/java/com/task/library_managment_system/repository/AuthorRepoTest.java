package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.Author;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Author Repos")
public class AuthorRepoTest {
    @Mock
    private AuthorRepo underTest;

    @Test
    @DisplayName("Check when name exist")
    void findByNameReturnsAuthorWhenAuthorExist(){
        Author author= Author.builder().
                name("Ahmed Khaled").
                id(1L).
                bio("He is a writer and was born in cairo.").
                build();
        when(underTest.findByName("Ahmed Khaled")).thenReturn(Optional.of(author));

        Optional<Author> resultOfFindByName=underTest.findByName("Ahmed Khaled");
        assertTrue(resultOfFindByName.isPresent());
        assertEquals(author.getName(),resultOfFindByName.get().getName());
    }
    @Test
    @DisplayName("Check that name of author not exist")
    void findByNameReturnsEmptyWhenAuthorDoesNotExist(){
        String notExistNameOfAuthor="Muhamed Ahmed";
        when(underTest.findByName(notExistNameOfAuthor)).thenReturn(Optional.empty());

        Optional<Author> resultOfFindByName=underTest.findByName(notExistNameOfAuthor);
        assertFalse(resultOfFindByName.isPresent());
    }


}
