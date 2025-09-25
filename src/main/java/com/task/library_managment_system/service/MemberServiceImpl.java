package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.member.MemberRequest;
import com.task.library_managment_system.dto.member.MemberResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.exception.member.MemberHasTransactionException;
import com.task.library_managment_system.models.Member;
import com.task.library_managment_system.reposatory.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
    private final MemberRepo memberRepo;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public MemberResponse createMember(MemberRequest request) {

        log.info("Check if Member exist or not...");
        if (memberRepo.findByName(request.getName()).isPresent()){
            log.warn("Member name:{}, already exist !",request.getName());
            throw new EntityFoundException("Member already exist !!");
        }

        Member member=Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .membershipDate(request.getMembershipDate()!=null?request.getMembershipDate(): LocalDate.now())
                .build();

        log.info("Member saved successfully.");
        memberRepo.save(member);

        return convertToMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public MemberResponse viewMemberById(Long memberId) {

        log.info("Check if Member that want to retrieve exist or not...");
        Member member =memberRepo.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("Member not found with id:"+memberId));

        return convertToMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STAFF')")
    public List<MemberResponse> viewAllMembers() {
        return memberRepo.findAll().stream()
                .map(this::convertToMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public MemberResponse updateMember(Long memberId, MemberRequest updatedMember) {

        log.info("Check if Member that want to update exist or not...");
        Member member =memberRepo.findById(memberId)
                .orElseThrow(()->new EntityNotFoundException("Member not found with id:"+memberId));

        if (updatedMember.getEmail() != null &&
                !updatedMember.getEmail().equals(member.getEmail())) {
            memberRepo.findByEmail(updatedMember.getEmail()).ifPresent(existingMember -> {
                if (!existingMember.getId().equals(memberId)) {
                    log.warn("Member email '{}' already used by another member", updatedMember.getEmail());
                    throw new EntityFoundException("Email already exists: " + updatedMember.getEmail());
                }
            });
        }
        if (updatedMember.getName() != null)    member.setName(updatedMember.getName());
        if (updatedMember.getEmail() != null)   member.setEmail(updatedMember.getEmail());
        if (updatedMember.getPhone() != null)   member.setPhone(updatedMember.getPhone());
        if (updatedMember.getMembershipDate() != null)  member.setMembershipDate(updatedMember.getMembershipDate());

        memberRepo.save(member);
        log.info("Member updated successfully.");

        return convertToMemberResponse(member) ;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMember(Long memberId) {
        Member member=memberRepo.findById(memberId)
                .orElseThrow(()-> new EntityNotFoundException("Member not found id:"+memberId));
        boolean hasBorrowingTransaction=!member.getTransactions().isEmpty();
        if (hasBorrowingTransaction){
            String errorMessage=String.format("Member '%s' with (id:%s) has one or more transaction associated"
                    ,member.getName(),memberId);
            log.warn(errorMessage);
            throw new MemberHasTransactionException(errorMessage);
        }

        memberRepo.delete(member);
        log.info("Member '{}' (ID: {}) deleted successfully.",
                member.getName(), memberId);

    }

    private MemberResponse convertToMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .membershipDate(member.getMembershipDate())
                .build();
    }
}
