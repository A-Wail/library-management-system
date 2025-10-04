package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.member.MemberRequest;
import com.task.library_managment_system.dto.member.MemberResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.member.MemberHasTransactionException;
import com.task.library_managment_system.models.BorrowingTransaction;
import com.task.library_managment_system.models.Member;
import com.task.library_managment_system.repository.MemberRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {
    @Mock private MemberRepo repo;
    @InjectMocks private MemberServiceImpl memberService;
    Member member;
    MemberRequest request;

    @BeforeEach
    void setUp() {
        member=Member.builder()
                .id(1L)
                .name("Muhamed Sad")
                .email("buda@gmail.com")
                .phone("1234567890")
                .membershipDate(LocalDate.of(2025,9,10))
                .transactions(Collections.emptyList())
                .build();
        request=MemberRequest.builder()
                .name("Muhamed Sad")
                .email("buda@gmail.com")
                .phone("1234567890")
                .membershipDate(LocalDate.of(2025,9,10))
                .build();
    }

    @Test
    void createMemberSuccess() {
        //when
        when(repo.findByName("Muhamed Sad")).thenReturn(Optional.empty());
        when(repo.save(any(Member.class))).thenReturn(member);
        //then
        MemberResponse response=memberService.createMember(request);
        //assert
        assertNotNull(response,"Response should not be null");
        assertEquals("Muhamed Sad",response.getName(),"Name should be match");
        verify(repo,times(1)).findByName("Muhamed Sad");
        verify(repo,times(1)).save(any(Member.class));
    }

    @Test
    void createMemberThrowExceptionWhenMemberExist() {
        //when
        when(repo.findByName("Muhamed Sad")).thenReturn(Optional.of(member));
        //then
        EntityFoundException foundException=assertThrows(EntityFoundException.class,
                                            ()->memberService.createMember(request),
                                            "Should throw EntityFoundException!");
         //assert
        assertEquals("Member already exist !!",foundException.getMessage(),"exception should match");
        verify(repo,times(1)).findByName("Muhamed Sad");
        verify(repo,never()).save(any(Member.class));
    }

    @Test
    void viewMemberByIdSuccess() {
        //when
        when(repo.findById(1L)).thenReturn(Optional.of(member));

        // Act
        MemberResponse response = memberService.viewMemberById(1L);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(1L, response.getId(), "ID should match");
        assertEquals("Muhamed Sad", response.getName(), "Name should match");
        verify(repo, times(1)).findById(1L);
    }

    @Test
    void viewMemberByIdThrowExceptionWhenNotExist() {
        //when
        when(repo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.viewMemberById(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Member not found with id:1", exception.getMessage());
        verify(repo, times(1)).findById(1L);
    }

    @Test
    void viewAllMembersSuccess() {
        //given
        Member member2 = Member.builder()
                .id(2L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .phone("0987654321")
                .membershipDate(LocalDate.of(2025, 9, 26))
                .transactions(Collections.emptyList())
                .build();
        when(repo.findAll()).thenReturn(Arrays.asList(member, member2));

        // Act
        List<MemberResponse> responses = memberService.viewAllMembers();

        // Assert
        assertEquals(2, responses.size(), "Should return two members");
        assertEquals("Muhamed Sad", responses.get(0).getName(), "First member name should match");
        assertEquals("Jane Doe", responses.get(1).getName(), "Second member name should match");
        verify(repo, times(1)).findAll();
    }

    @Test
    void viewAllMembersReturnsEmptyListWhenNoMembers() {
        // Arrange
        when(repo.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<MemberResponse> responses = memberService.viewAllMembers();

        // Assert
        assertTrue(responses.isEmpty(), "Should return empty list");
        verify(repo, times(1)).findAll();
    }


    @Test
    void updateMemberSuccess() {
        MemberRequest updateRequest = MemberRequest.builder()
                .name("Jon Doe")
                .email("jondoe@example.com")
                .phone("0987654321")
                .membershipDate(LocalDate.of(2025, 9, 26))
                .build();
        Member updatedMember = Member.builder()
                .id(1L)
                .name("Jon Doe")
                .email("jondoe@example.com")
                .phone("0987654321")
                .membershipDate(LocalDate.of(2025, 9, 26))
                .transactions(Collections.emptyList())
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(member));
        when(repo.findByEmail("jondoe@example.com")).thenReturn(Optional.empty());
        when(repo.save(any(Member.class))).thenReturn(updatedMember);

        // Act
        MemberResponse response = memberService.updateMember(1L, updateRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(1L, response.getId(), "ID should match");
        assertEquals("jondoe@example.com", response.getEmail(), "Email should match");
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).findByEmail("jondoe@example.com");
        verify(repo, times(1)).save(any(Member.class));
    }

    @Test
    void updateMemberThrowsEntityNotFoundExceptionWhenMemberNotFound(){
        //when
        when(repo.findById(1L)).thenReturn(Optional.empty());
        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.updateMember(1L, request),
                "Should throw EntityNotFoundException");
        assertEquals("Member not found with id:1", exception.getMessage());
        verify(repo, times(1)).findById(1L);
        verify(repo, never()).findByEmail(anyString());
        verify(repo, never()).save(any(Member.class));
    }
    @Test
    void updateMemberThrowsEntityFoundExceptionWhenMemberEmailUsed(){
        //given
        MemberRequest updateRequest = MemberRequest.builder()
                .name("Jane Doe")
                .email("jondoe@example.com")
                .phone("0987654321")
                .build();
        Member existingMember = Member.builder()
                .id(2L)
                .name("Other Member")
                .email("jondoe@example.com")
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(member));
        when(repo.findByEmail("jondoe@example.com")).thenReturn(Optional.of(existingMember));

        // Act & Assert
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> memberService.updateMember(1L, updateRequest),
                "Should throw EntityFoundException");
        assertEquals("Email already exists: jondoe@example.com", exception.getMessage());
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).findByEmail("jondoe@example.com");
        verify(repo, never()).save(any(Member.class));
    }

    @Test
    void deleteMemberSuccess() {
        // given
        when(repo.findById(1L)).thenReturn(Optional.of(member));
        doNothing().when(repo).delete(member);

        // then
        memberService.deleteMember(1L);

        // Assert
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).delete(member);
    }

    @Test
    void deleteMemberThrowsEntityNotFoundExceptionWhenMemberNotFound() {
        // given
        when(repo.findById(1L)).thenReturn(Optional.empty());

        // then & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.deleteMember(1L),
                "Should throw EntityNotFoundException");
        assertEquals("Member not found id:1", exception.getMessage());
        verify(repo, times(1)).findById(1L);
        verify(repo, never()).delete(any(Member.class));
    }

    @Test
    void deleteMemberThrowsMemberHasTransactionExceptionWhenMemberHasTransactions() {
        // Arrange
        Member memberWithTransactions = Member.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .membershipDate(LocalDate.of(2025, 9, 25))
                .transactions(List.of(new BorrowingTransaction()))
                .build();
        when(repo.findById(1L)).thenReturn(Optional.of(memberWithTransactions));

        // Act & Assert
        MemberHasTransactionException exception = assertThrows(MemberHasTransactionException.class,
                                                    () -> memberService.deleteMember(1L),
                                                    "Should throw MemberHasTransactionException");
        assertTrue(exception.getMessage().contains("Member 'John Doe' with (id:1) has one or more transaction associated"));
        verify(repo, times(1)).findById(1L);
        verify(repo, never()).delete(any(Member.class));
    }
}