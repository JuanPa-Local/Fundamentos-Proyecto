package com.openlib.backend.domain.user;

import com.openlib.backend.domain.user.exception.CuentaInactivaException;
import com.openlib.backend.domain.user.exception.CredencialesInvalidasException;
import com.openlib.backend.domain.user.exception.EmailDuplicadoException;
import com.openlib.backend.domain.user.exception.UsuarioNoEncontradoException;
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

    // US-004: Registro de Buyer
    public User register(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException(email);
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.BUYER);
        return userRepository.save(user);
    }

    // US-005: Inicio de sesión con validación de cuenta activa
    public User login(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new CredencialesInvalidasException();
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        // US-005: Validar que la cuenta esté activa
        if (!user.isActive()) {
            throw new CuentaInactivaException(email);
        }

        return user;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String generarHash(String password) {
        return passwordEncoder.encode(password);
    }

    // US-006: Registro de Seller
    public User registerSeller(String fullName, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException(email);
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.SELLER);
        return userRepository.save(user);
    }

    // US-007: Actualizar perfil de usuario
    public User updateProfile(UUID userId, String fullName, String phone, String address) {
        User user = getUserById(userId);
        user.updateProfile(fullName, phone, address);
        return userRepository.save(user);
    }

    // US-007: Ver perfil de usuario
    public User getProfile(UUID userId) {
        return getUserById(userId);
    }

    // US-008: Listar usuarios por rol
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(User.Role.valueOf(role.toUpperCase()));
    }

    // US-008: Activar cuenta de usuario
    public User activateUser(UUID id) {
        User user = getUserById(id);
        user.activate();
        return userRepository.save(user);
    }

    // US-008: Desactivar cuenta de usuario
    public User deactivateUser(UUID id) {
        User user = getUserById(id);
        user.deactivate();
        return userRepository.save(user);
    }
}