package com.task.library_managment_system.controller;


import com.task.library_managment_system.dto.systemUser.RequestUser;
import com.task.library_managment_system.dto.systemUser.UserResponse;
import com.task.library_managment_system.service.SystemUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final SystemUserService userService;

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@RequestBody RequestUser request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody RequestUser request) {
        return ResponseEntity.ok(userService.updateUser(id,request));
    }

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAll(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id){
     return ResponseEntity.ok(userService.returnUserById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}


