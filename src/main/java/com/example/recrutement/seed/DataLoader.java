package com.example.recrutement.seed;

import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createUserIfNotExists(
                "RH@example.com",
                "123456",
                Role.RH
        );

        createUserIfNotExists(
                "admin@example.com",
                "123456",
                Role.ADMIN_MB
        );

        createUserIfNotExists(
                "candidat@example.com",
                "123456",
                Role.CANDIDAT
        );
    }

    private void createUserIfNotExists(
            String email,
            String password,
            Role role
    ) {

        if (userRepository.findByEmail(email).isEmpty()) {

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);

            userRepository.save(user);

            System.out.println(
                    "✅ User created : " + email +
                    " | ROLE : " + role
            );
        }
    }
}