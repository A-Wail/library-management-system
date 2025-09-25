package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.publisher.PublisherRequest;
import com.task.library_managment_system.dto.publisher.PublisherResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.publisher.PublisherAssociatedBooksException;
import com.task.library_managment_system.models.Publisher;
import com.task.library_managment_system.reposatory.PublisherRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService{
    private final PublisherRepo publisherRepo;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PublisherResponse createPublisher(PublisherRequest request) {
        log.info("Check if publisher exist or not...");
        if (publisherRepo.findByName(request.getName()).isPresent()){
            log.info("Publisher name:{}, already exist !",request.getName());
            throw new EntityFoundException("Publisher already exist !!");
        }
        Publisher publisher=Publisher.builder()
                .name(request.getName())
                .address(request.getAddress())
                .build();

        publisherRepo.save(publisher);
        log.info("Publisher saved successfully.");

        return convertToResponse(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public PublisherResponse viewPublisherById(Long publisherId) {
        log.info("Check if publisher exist to view or not...");
        Publisher publisher=publisherRepo.findById(publisherId).orElseThrow(
                ()->new EntityNotFoundException("Publisher not found with id:"+publisherId));
        return convertToResponse(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public List<PublisherResponse> viewAllPublisher() {
        return publisherRepo.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PublisherResponse updatePublisher(Long publisherId, PublisherRequest updatedPublisher) {

        log.info("Check if publisher exist to view or not...");
        Publisher publisher=publisherRepo.findById(publisherId)
                .orElseThrow(()->new EntityNotFoundException("Publisher not found with id:"+publisherId));

        if (!publisher.getName().equals(updatedPublisher.getName())&&
                publisherRepo.findByName(updatedPublisher.getName()).isPresent()){
            log.warn("Publisher already exist before");
            throw new EntityFoundException("Publisher already exist: "+publisher.getName());
        }
        log.info("Publisher is being updated...");
        if (updatedPublisher.getName() != null) publisher.setName(updatedPublisher.getName());
        if (updatedPublisher.getAddress() != null) publisher.setAddress(updatedPublisher.getAddress());
        publisherRepo.save(publisher);
        log.info("Publisher updated successfully.");

        return convertToResponse(publisher) ;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePublisher(Long publisherId) {
        Publisher publisher=publisherRepo.findById(publisherId).
                orElseThrow(()->new EntityNotFoundException("Publisher Not found id: "+publisherId));
        log.info("Check if author with id {} has books or not ",publisherId);
        boolean hasBooks=!publisher.getBooks().isEmpty();
        if (hasBooks){
            String errorMessage=String.format("Publisher '%s' with (id:%s) has one or more books associated with them"
                    ,publisher.getName(),publisherId);
            log.warn(errorMessage);
            throw new PublisherAssociatedBooksException(errorMessage);
        }
        publisherRepo.delete(publisher);
        log.info("Publisher '{}' (ID: {}) deleted successfully.",
                publisher.getName(), publisherId);
    }

    private PublisherResponse convertToResponse(Publisher publisher) {
        return PublisherResponse.builder()
                .address(publisher.getAddress())
                .name(publisher.getName())
                .build();
    }
}
