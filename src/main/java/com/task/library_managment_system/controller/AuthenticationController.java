package com.task.library_managment_system.controller;

import com.task.library_managment_system.dto.author.AuthorResponse;
import com.task.library_managment_system.dto.systemUser.AuthenticationRequest;
import com.task.library_managment_system.dto.systemUser.AuthenticationResp;
import com.task.library_managment_system.dto.systemUser.RequestUser;
import com.task.library_managment_system.security.JwtService;
import com.task.library_managment_system.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserServiceImpl userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResp> register(@RequestBody RequestUser requestUser) {
        return ResponseEntity.ok(userService.register(requestUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResp> login(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(userService.authenticate(request));
    }

}
