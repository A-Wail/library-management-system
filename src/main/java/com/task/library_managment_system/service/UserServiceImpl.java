package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.systemUser.AuthenticationRequest;
import com.task.library_managment_system.dto.systemUser.AuthenticationResp;
import com.task.library_managment_system.dto.systemUser.RequestUser;
import com.task.library_managment_system.dto.systemUser.UserResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.models.SystemUser;
import com.task.library_managment_system.reposatory.SystemUserRepo;
import com.task.library_managment_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements SystemUserService {
    private final SystemUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AuthenticationResp register(RequestUser request){
        log.info("Check if User: {}, registered successfully", request.getUsername());
        if (userRepo.findByUsername(request.getUsername()).isPresent())
            throw new EntityFoundException("User is Register before ");

        SystemUser user =SystemUser.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .hashPass(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepo.save(user);

        String token= jwtService.generateToken(user);

        log.info("User '{}' registered successfully", user.getUsername());

        return AuthenticationResp.builder()
                .token(token)
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public AuthenticationResp authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepo.findByUsername(request.getUsername())
                .orElseThrow( ()-> new EntityNotFoundException("User not found !!"));

        log.info("User Founded");

        var token= jwtService.generateToken(user);

        return AuthenticationResp.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(RequestUser requestUser) {
        log.info("check if username unique or not ...");
        if (userRepo.findByUsername(requestUser.getUsername()).isPresent()){
            log.warn("User with username:{}, already exist !",requestUser.getUsername());
            throw new EntityFoundException("Username already exist !!");
        }

        log.info("check if email unique or not ...");
        if (userRepo.findByUsername(requestUser.getUsername()).isPresent()){
            log.warn("email :{}, already exist !",requestUser.getEmail());
            throw new EntityFoundException("Email already exist !!");
        }

        log.info("The password will now be encrypted...");
        SystemUser user=SystemUser.builder()
                .email(requestUser.getEmail())
                .username(requestUser.getUsername())
                .role(requestUser.getRole())
                .hashPass(passwordEncoder.encode(requestUser.getPassword()))
                .build();
        log.info("User will now be saving...");

        userRepo.save(user);

        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::convertToUserResponse).
                collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or authentication.principal.id == #userId")
    public UserResponse returnUserById(Long userId) {
        SystemUser user =userRepo.findById(userId).orElseThrow(()->
                new EntityNotFoundException("User not found:"+userId));
        return convertToUserResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or" +
            " (authentication.principal.id == #userId and not #requestUser.role.name() == 'ADMIN')")
    public UserResponse updateUser(Long userId, RequestUser requestUser) {
        log.info("Updating user with ID: {}", userId);
        SystemUser user =userRepo.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User not found:"+userId));
        // Prevent non-ADMINS from changing roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && requestUser.getRole() != user.getRole()) {
            log.warn("Non-admin user {} attempted to change role", auth.getName());
            throw new SecurityException("Only admins can change roles");
        }

        log.info("check if username unique or used by other user ...");
        if (!user.getUsername().equals(requestUser.getUsername())&&
            userRepo.findByUsername(requestUser.getUsername()).isPresent()){
            log.warn("Username'{}' is already used ...",requestUser.getUsername());
            throw new EntityFoundException("Username already used by other user change it ");
        }
        log.info("check if email unique or used by other user ...");
        if (!user.getEmail().equals(requestUser.getEmail())&&
                userRepo.findByEmail(requestUser.getEmail()).isPresent()){
            log.warn("Email'{}' is already used ...",requestUser.getEmail());
            throw new EntityFoundException("Email already used by other user change it ");
        }

        if (requestUser.getUsername() != null) user.setUsername(requestUser.getUsername());
        if (requestUser.getEmail() != null) user.setEmail(requestUser.getEmail());

        log.info("Username and email updated successfully ! ...");
        log.info("check if there is new password or used old password ...");
        if (requestUser.getUsername() != null && !requestUser.getPassword().isEmpty()){
            user.setHashPass(passwordEncoder.encode(requestUser.getPassword()));
            log.info("Password updated successfully! ...");
        }
        if (requestUser.getRole() != null) user.setRole( requestUser.getRole());
        log.info("User will now be saving...");
        userRepo.save(user);
        return convertToUserResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepo.existsById(userId)){
            log.warn("User (id:{}) not found",userId);
            throw new EntityNotFoundException("User not found to delete !");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SystemUser currentUser = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        if (currentUser.getId().equals(userId)) {
            log.warn("Admin {} attempted to delete themselves", auth.getName());
            throw new SecurityException("Cannot delete your own account");
        }

        userRepo.deleteById(userId);
        log.info("User with (ID: {})deleted successfully.",userId);
    }
    private UserResponse convertToUserResponse(SystemUser user){
        return UserResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

}
