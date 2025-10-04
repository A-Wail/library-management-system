package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.publisher.PublisherRequest;
import com.task.library_managment_system.dto.publisher.PublisherResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.publisher.PublisherAssociatedBooksException;
import com.task.library_managment_system.models.Book;
import com.task.library_managment_system.models.Publisher;
import com.task.library_managment_system.repository.PublisherRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceImplTest {
    @Mock private PublisherRepo publisherRepo;
    @InjectMocks private PublisherServiceImpl publisherService;
    private Publisher publisher;
    private PublisherRequest request;

    @BeforeEach
    void setUp() {
        publisher=Publisher.builder()
                .id(1L)
                .name("Nader Fouda")
                .address("Cairo,Nasir city")
                .books(Collections.emptyList())
                .build();
        request=PublisherRequest.builder()
                .name("Nader Fouda")
                .address("Cairo,Nasir city")
                .build();
    }

    @Test
    void createPublisherSuccess() {
        //when
        when(publisherRepo.findByName("Nader Fouda")).thenReturn(Optional.empty());
        when(publisherRepo.save(any(Publisher.class))).thenReturn(publisher);
        //then & assert
        PublisherResponse response=publisherService.createPublisher(request);
        assertNotNull(response, "Response should not be null");
        assertEquals("Nader Fouda", response.getName(), "Name should match");
        assertEquals("Cairo,Nasir city", response.getAddress(), "Address should match");
        verify(publisherRepo, times(1)).findByName("Nader Fouda");
        verify(publisherRepo, times(1)).save(any(Publisher.class));

    }

    @Test
    void createPublisher_throwEntityFoundException_whenPublisherNameExist(){
        //when
        when(publisherRepo.findByName("Nader Fouda")).thenReturn(Optional.of(publisher));
        //then & assert
        EntityFoundException foundException=assertThrows(EntityFoundException.class,
                ()->publisherService.createPublisher(request),
                "Should throw EntityFoundException");
        assertEquals("Publisher already exist !!",foundException.getMessage());
        verify(publisherRepo,times(1)).findByName("Nader Fouda");
        verify(publisherRepo,never()).save(any(Publisher.class));
    }

    @Test
    void viewPublisherByIdSuccess() {
        //when
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisher));
        //then & assert
        PublisherResponse response=publisherService.viewPublisherById(1L);
        assertNotNull(response, "Response should not be null");
        assertEquals("Nader Fouda", response.getName(), "Name should match");
        assertEquals("Cairo,Nasir city", response.getAddress(), "Address should match");
        verify(publisherRepo, times(1)).findById(1L);

    }

    @Test
    void viewPublisherById_throwsEntityNotFoundException_whenPublisherNotFound() {
        // when
        when(publisherRepo.findById(1L)).thenReturn(Optional.empty());

        // then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> publisherService.viewPublisherById(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Publisher not found with id:1", exception.getMessage());
        verify(publisherRepo, times(1)).findById(1L);
    }

    @Test
    void viewAllPublisherSuccess() {
        //given
        Publisher publisher2 = Publisher.builder()
                .id(2L)
                .name("Random House")
                .address("456 Book Ave")
                .books(Collections.emptyList())
                .build();
        when(publisherRepo.findAll()).thenReturn(Arrays.asList(publisher, publisher2));

        // Act
        List<PublisherResponse> responses = publisherService.viewAllPublisher();

        // Assert
        assertEquals(2, responses.size(), "Should return two publishers");
        assertEquals("Nader Fouda", responses.get(0).getName(), "First publisher name should match");
        assertEquals("Random House", responses.get(1).getName(), "Second publisher name should match");
        verify(publisherRepo, times(1)).findAll();
    }



    @Test
    void updatePublisherSuccess() {
        //given
        PublisherRequest random_publisher = PublisherRequest.builder()
                .name("Random publisher")
                .address("456 Book Ave")
                .build();
        Publisher updatedPublisher = Publisher.builder()
                .id(1L)
                .name("Random publisher")
                .address("456 Book Ave")
                .books(Collections.emptyList())
                .build();
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepo.findByName("Random publisher")).thenReturn(Optional.empty());
        when(publisherRepo.save(any(Publisher.class))).thenReturn(updatedPublisher);

        // then
        PublisherResponse response = publisherService.updatePublisher(1L, random_publisher);

        // assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Random publisher", response.getName(), "Name should match");
        assertEquals("456 Book Ave", response.getAddress(), "Address should match");
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, times(1)).findByName("Random publisher");
        verify(publisherRepo, times(1)).save(any(Publisher.class));
    }

    @Test
    void updatePublisher_throwsEntityNotFoundException_whenPublisherNotFound() {
        // when
        when(publisherRepo.findById(1L)).thenReturn(Optional.empty());

        // then & assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> publisherService.updatePublisher(1L, request),
                "Should throw EntityNotFoundException");
        assertEquals("Publisher not found with id:1", exception.getMessage());
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, never()).findByName(anyString());
        verify(publisherRepo, never()).save(any(Publisher.class));
    }

    @Test
    void updatePublisher_throwsEntityFoundException_whenNameExists() {
        // given
        PublisherRequest updateRequest = PublisherRequest.builder()
                .name("Random publisher")
                .address("456 Book Ave")
                .build();
        Publisher existingPublisher = Publisher.builder()
                .id(2L)
                .name("Random publisher")
                .address("789 Publisher Rd")
                .build();
        //when
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepo.findByName("Random publisher")).thenReturn(Optional.of(existingPublisher));

        // then & assert
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> publisherService.updatePublisher(1L, updateRequest),
                "Should throw EntityFoundException");
        assertEquals("Publisher already exist!", exception.getMessage());
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, times(1)).findByName("Random publisher");
        verify(publisherRepo, never()).save(any(Publisher.class));
    }

    @Test
    void deletePublisherSuccess() {
        // given
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisher));
        doNothing().when(publisherRepo).delete(publisher);

        // Act
        publisherService.deletePublisher(1L);

        // Assert
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, times(1)).delete(publisher);
    }

    @Test
    void deletePublisher_throwsEntityNotFoundException_whenPublisherNotFound() {
        // Arrange
        when(publisherRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> publisherService.deletePublisher(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Publisher Not found id: 1", exception.getMessage());
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, never()).delete(any(Publisher.class));
    }

    @Test
    void deletePublisher_throwsPublisherAssociatedBooksException_whenPublisherHasBooks() {
        // Arrange
        Publisher publisherWithBooks = Publisher.builder()
                .id(1L)
                .name("Nader Fouda")
                .address("Cairo, Nasir city")
                .books(List.of(new Book()))
                .build();
        when(publisherRepo.findById(1L)).thenReturn(Optional.of(publisherWithBooks));

        // Act & Assert
        PublisherAssociatedBooksException exception = assertThrows(PublisherAssociatedBooksException.class,
                () -> publisherService.deletePublisher(1L),
                "Should throw PublisherAssociatedBooksException");
        assertTrue(exception.getMessage().contains("Publisher 'Nader Fouda' with (id:1) has one or more books associated"));
        verify(publisherRepo, times(1)).findById(1L);
        verify(publisherRepo, never()).delete(any(Publisher.class));
    }
}