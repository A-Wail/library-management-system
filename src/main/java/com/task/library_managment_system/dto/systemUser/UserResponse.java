package com.task.library_managment_system.dto.systemUser;

import com.task.library_managment_system.models.SystemUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String email;
    private String username;
    private SystemUser.Role role;
}
