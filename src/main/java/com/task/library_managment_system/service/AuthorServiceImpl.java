package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.author.AuthorResponse;
import com.task.library_managment_system.dto.author.RequestAuthor;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.author.AuthorAssociatedBooksException;
import com.task.library_managment_system.models.Author;
import com.task.library_managment_system.repository.AuthorRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService{

    private final AuthorRepo authorRepo;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public AuthorResponse addAuthor(RequestAuthor newAuthor) {
        //check if author exist before or not
        if (authorRepo.findByName(newAuthor.getName()).isPresent()){
            log.info("Author name:{}, already exist !",newAuthor.getName());
            throw new EntityFoundException("Author already exist !!");
        }
        Author author=Author.builder()
                .bio(newAuthor.getBiography())
                .name(newAuthor.getName())
                .build();
        Author savedAuthor=authorRepo.save(author);

        return convertToAuthorResponse(savedAuthor);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public AuthorResponse updateAuthor(Long id,RequestAuthor updateAuthor) {

        log.info("check if author exist to retrieve...");
        Author author =authorRepo.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Author not found!"));
        log.info("check if author name used by another user or not...");
        if (!author.getName().equals(updateAuthor.getName())&&
            authorRepo.findByName(updateAuthor.getName()).isPresent()){
            log.warn("Author name that insert exist before");
            throw new EntityFoundException("Author name already existed");
        }

        log.info("Author is being updated...");
        if (updateAuthor.getName() != null)         author.setName(updateAuthor.getName());
        if (updateAuthor.getBiography() != null)    author.setBio(updateAuthor.getBiography());
        authorRepo.save(author);
        log.info("Author updated successfully.");

        return convertToAuthorResponse(author);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public List<AuthorResponse> viewAuthors() {

        return authorRepo.findAll().stream()
                .map(this::convertToAuthorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public AuthorResponse viewAuthorById(Long authorId) {
        log.info("check if author exist to retrieve...");
        Author author =authorRepo.findById(authorId)
                .orElseThrow(()->new EntityNotFoundException("Author not found!"));

        return convertToAuthorResponse(author);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAuthor(Long authorId) {
        Author author=authorRepo.findById(authorId).
                orElseThrow(()->new EntityNotFoundException("Author Not found id: "+authorId));
        log.info("Check if author with id {} has books or not ",authorId);
        boolean hasBooks=!author.getBooks().isEmpty();
        if (hasBooks){
            String errorMessage=String.format("author '%s' with (id:%s) has one or more books associated with them"
                    ,author.getName(),authorId);
            log.warn(errorMessage);
            throw new AuthorAssociatedBooksException(errorMessage);
        }
        authorRepo.delete(author);
        log.info("Author '{}' (ID: {}) deleted successfully.",
                author.getName(), authorId);
    }
    private AuthorResponse convertToAuthorResponse(Author author){
        return AuthorResponse.builder().id(author.getId()).name(author.getName())
                .biography(author.getBio()).build();
    }
}
