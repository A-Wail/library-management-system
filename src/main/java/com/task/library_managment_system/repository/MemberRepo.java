package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepo extends BaseRepo<Member,Long>{
    Optional<Member> findByEmail(String email);
}
