// com.example.recrutement.notification.repository.NotificationPreferenceRepository.java
package com.example.recrutement.notification.repository;

import com.example.recrutement.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}