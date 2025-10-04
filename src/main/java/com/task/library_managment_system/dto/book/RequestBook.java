package com.task.library_managment_system.dto.book;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class RequestBook {
    @NotBlank(message = "ISBN is required")
    private String isbn;
    @NotBlank(message = "Book title is required")
    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;
    @NotNull(message = "Publication year is required")
    @Min(value = 1900, message = "Publication year must be after 1900") // First printing press
    @Max(value = 2025, message = "Publication year cannot be in the future")
    private Integer publicationYear;
    @NotBlank(message = "Edition information is required")
    private String edition;
    @Size(max = 2000, message = "Summary cannot exceed 2000 characters")
    private String summary;
    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 50, message = "Language must be between 2 and 50 characters")
    private String language;
    @URL(message = "Cover URL must be a valid URL")
    private String coverUrl;
}
