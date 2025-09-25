package com.task.library_managment_system.dto.author;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AuthorResponse {
    Long id;
    String name;
    String biography;

}
