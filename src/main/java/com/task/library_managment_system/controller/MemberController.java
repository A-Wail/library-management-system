package com.task.library_managment_system.controller;


import com.task.library_managment_system.dto.member.MemberRequest;
import com.task.library_managment_system.dto.member.MemberResponse;
import com.task.library_managment_system.models.Member;
import com.task.library_managment_system.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping()
    public ResponseEntity<MemberResponse> insert(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id,request));
    }

    @GetMapping()
    public ResponseEntity<List<MemberResponse>> getAll(){
        return ResponseEntity.ok(memberService.viewAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id){
     return ResponseEntity.ok(memberService.viewMemberById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Long id){
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }


}


