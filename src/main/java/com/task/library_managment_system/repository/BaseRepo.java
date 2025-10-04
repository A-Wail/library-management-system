package com.task.library_managment_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepo <T,ID> extends JpaRepository<T,ID> {
    Optional<T> findByName(String name);

}
