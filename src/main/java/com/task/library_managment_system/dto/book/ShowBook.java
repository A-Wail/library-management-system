package com.task.library_managment_system.dto.book;

import com.task.library_managment_system.LibraryManagementSystemApplication;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ShowBook {
    private String isbn;
    private String title;
    private Integer publicationYear;
    private String edition;
    private String summary;
    private String language;
    private String coverUrl;
    private Long publisherId;
    private List<Long> authorIds;
    private List<String> categoriesName;

}
