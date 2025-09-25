package com.task.library_managment_system.dto.member;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate membershipDate;
}
