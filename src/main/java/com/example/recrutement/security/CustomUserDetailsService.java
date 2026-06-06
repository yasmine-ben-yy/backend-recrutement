package com.example.recrutement.security;

import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        System.out.println("User loaded: " + user.getEmail() + " | Role: " + user.getRole());
        
        // Convertir Role enum en format Spring Security (ROLE_XXX)
        String roleWithPrefix = "ROLE_" + user.getRole().name();
        
        System.out.println("🎯 Granted authority: " + roleWithPrefix);
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final User user;
        
        public CustomUserDetails(User user) {
            super(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            this.user = user;
        }
        
        public User getUser() {
            return user;
        }
    }
}