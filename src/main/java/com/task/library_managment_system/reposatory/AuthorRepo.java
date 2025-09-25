package com.task.library_managment_system.reposatory;

import com.task.library_managment_system.models.Author;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepo extends BaseRepo<Author,Long>{
}
