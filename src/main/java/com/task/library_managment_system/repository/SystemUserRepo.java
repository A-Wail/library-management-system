package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemUserRepo extends JpaRepository<SystemUser,Long> {

    Optional<SystemUser> findByEmail(String email);
    Optional<SystemUser> findByUsername(String username);
}
