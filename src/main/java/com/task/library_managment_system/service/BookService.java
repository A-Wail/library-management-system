package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.book.OverviewBook;
import com.task.library_managment_system.dto.book.RequestBook;
import com.task.library_managment_system.dto.book.ShowBook;
import java.util.List;

public interface BookService {
    ShowBook createBook(RequestBook book,List<Long> categoryIds, Long publisherId,List<Long> authorIds);
    ShowBook searchBookByIsbn(String isbn);
    List<OverviewBook> overviewBooks();
    ShowBook modifyBookDetails(Long bookId, RequestBook book, List<Long> categoryIds, Long publisherId, List<Long> authorIds);
    void removeBook(Long bookId);
    boolean isBookAvailable(Long bookId);

}
