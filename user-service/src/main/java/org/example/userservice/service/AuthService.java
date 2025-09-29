package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.config.JwtUtil;
import org.example.userservice.exceptions.UserExistsException;
import org.example.userservice.model.Role;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(String name, String email, String password, Role role){
        if (userRepository.findByEmail(email).isPresent()) throw new UserExistsException("User exists");
        Set<Role> roles = Set.of(role);
        User user = new User(null,name,email,passwordEncoder.encode(password),roles);
        userRepository.save(user);
        return "Registered Successfully";
    }

    public String login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new BadCredentialsException("Bad credentials");

        return jwtUtil.generateToken(user.getUsername());
    }
}
