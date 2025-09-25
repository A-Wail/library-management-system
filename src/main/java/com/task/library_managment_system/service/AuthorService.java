package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.author.AuthorResponse;
import com.task.library_managment_system.dto.author.RequestAuthor;
import com.task.library_managment_system.models.Author;

import java.util.List;

public interface AuthorService {
    AuthorResponse addAuthor(RequestAuthor newAuthor);
    AuthorResponse updateAuthor(Long id, RequestAuthor authorToUpdate);
    List<AuthorResponse> viewAuthors();
    AuthorResponse viewAuthorById(Long authorId);
    void deleteAuthor(Long authorId);
}
