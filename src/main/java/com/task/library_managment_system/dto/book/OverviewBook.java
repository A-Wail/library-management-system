package com.task.library_managment_system.dto.book;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverviewBook {
    String isbn;
    String title;
}
