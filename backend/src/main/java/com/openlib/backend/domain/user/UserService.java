package com.openlib.backend.domain.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado: " + email);
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.BUYER);
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("DEBUG: Usuario no encontrado: " + email);
            return Optional.empty();
        }

        User user = userOpt.get();
        System.out.println("DEBUG: Hash en BD: " + user.getPasswordHash());
        System.out.println("DEBUG: Password recibido: " + rawPassword);
        boolean match = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        System.out.println("DEBUG: Match: " + match);

        return match ? Optional.of(user) : Optional.empty();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String generarHash(String password) {
        return passwordEncoder.encode(password);
    }
}