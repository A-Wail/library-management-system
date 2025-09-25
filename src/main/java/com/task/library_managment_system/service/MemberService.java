package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.member.MemberRequest;
import com.task.library_managment_system.dto.member.MemberResponse;
import com.task.library_managment_system.models.Member;

import java.util.List;

public interface MemberService {
    MemberResponse createMember(MemberRequest member);
    MemberResponse viewMemberById(Long memberId);
    List<MemberResponse> viewAllMembers();
    MemberResponse updateMember(Long memberId, MemberRequest updatedMember);
    void deleteMember(Long memberId);
}
