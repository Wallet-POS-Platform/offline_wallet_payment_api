package com.pixnetics.wallet_backend.payment.user;

import com.pixnetics.wallet_backend.common.enums.UserRole;
import com.pixnetics.wallet_backend.payment.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.MANDATORY)
    public User findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public User findByUserName(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public User getSystemUser(){
        return userRepository.findSystemUser()
                .orElseGet(this::createSystemUser);
    }

    private User createSystemUser() {
        User user = new User();
        user.setUsername("system_user");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(UserRole.SYSTEM);
        user.setActive(true);

        return userRepository.save(user);
    }
}