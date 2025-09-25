package com.task.library_managment_system.dto.systemUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class AuthenticationResp {
    private String token;
}
