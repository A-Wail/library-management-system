package com.task.library_managment_system.repository;

import com.task.library_managment_system.models.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberRepoTest {
    @Mock private MemberRepo repo;

    @Test
    @DisplayName("Check that email of member exist")
    void findMemberByEmailWhenExist(){
        //given
        String email="buda@gmial.com";
        Member member= Member.builder()
                .id(1L)
                .email(email)
                .phone("+201000000055")
                .build();
        //when
        when(repo.findByEmail(email)).thenReturn(Optional.of(member));
        Optional<Member> resultOfSearch=repo.findByEmail(email);
        //then
        assertTrue(resultOfSearch.isPresent());
        assertEquals(member.getEmail(),resultOfSearch.get().getEmail());
    }
    @Test
    @DisplayName("Check that email of member doesn't exist")
    void findMemberByEmailWhenDoesNotExist(){
        //given
        String email="buda@gmial.com";
        //when
        when(repo.findByEmail(email)).thenReturn(Optional.empty());
        Optional<Member> resultOfSearch=repo.findByEmail(email);
        //then
        assertFalse(resultOfSearch.isPresent());
    }
}