package com.task.library_managment_system.dto.book;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;


@Data
public class CreateBookRequest {
    @Valid
    private RequestBook bookDetails;

    @NotEmpty
    private List<Long> categoryIds;

    @NotNull
    private Long publisherId;

    @NotEmpty
    private List<Long> authorIds;
}
