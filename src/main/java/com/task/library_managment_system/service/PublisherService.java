package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.publisher.PublisherRequest;
import com.task.library_managment_system.dto.publisher.PublisherResponse;
import com.task.library_managment_system.models.Publisher;
import java.util.List;


public interface PublisherService {
    PublisherResponse createPublisher(PublisherRequest publisher);
    PublisherResponse viewPublisherById(Long publisherId);
     List<PublisherResponse> viewAllPublisher();
    PublisherResponse updatePublisher(Long publisherId, PublisherRequest updatedPublisher);
     void deletePublisher(Long publisherId);
}
