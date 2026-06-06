// com.example.recrutement.user.repository.UserRepository.java
package com.example.recrutement.user.repository;

import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByEnabled(boolean enabled);
Optional<User> findByVerificationToken(String token);
    
    Optional<User> findByResetPasswordToken(String token);

}