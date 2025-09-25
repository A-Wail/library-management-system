package com.task.library_managment_system.dto.publisher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublisherResponse {
    private String name;
    private String address;
}
