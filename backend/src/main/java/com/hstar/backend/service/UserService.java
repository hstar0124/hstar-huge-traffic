package com.hstar.backend.service;

import com.hstar.backend.dto.SignUpUserRequest;
import com.hstar.backend.entity.User;
import com.hstar.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(SignUpUserRequest signUpUserRequest) {
        User user = new User();
        user.setUsername(signUpUserRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpUserRequest.getPassword()));
        user.setEmail(signUpUserRequest.getEmail());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }
}