package com.task.library_managment_system.dto.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class RequestAuthor {
    @NotBlank
    @Size(min = 3, max = 30,
            message = "Name must be between 3 and 30 characters")
    String name;
    @NotEmpty
    @Size(min = 1, max = 200,
            message = "Biography must be between 1 and 200 characters")
    String biography;
}
