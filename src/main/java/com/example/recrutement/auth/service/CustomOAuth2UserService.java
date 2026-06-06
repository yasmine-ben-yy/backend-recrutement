// com.example.recrutement.auth.service.CustomOAuth2UserService.java
package com.example.recrutement.auth.service;

import com.example.recrutement.auth.dto.OAuth2UserInfo;
import com.example.recrutement.candidate.entity.CandidateProfile;
import com.example.recrutement.candidate.repository.CandidateProfileRepository;
import com.example.recrutement.user.entity.Role;
import com.example.recrutement.user.entity.User;
import com.example.recrutement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("🔐 OAuth2 Login via: {}", registrationId);

        OAuth2UserInfo userInfo = extractUserInfo(registrationId, oAuth2User.getAttributes());
        
        User user = processOAuth2User(userInfo);
        log.info("✅ Utilisateur traité: {}", user.getEmail());
        
        return oAuth2User;
    }

    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return OAuth2UserInfo.builder()
                    .id((String) attributes.get("sub"))
                    .email((String) attributes.get("email"))
                    .name((String) attributes.get("name"))
                    .givenName((String) attributes.get("given_name"))
                    .familyName((String) attributes.get("family_name"))
                    .pictureUrl((String) attributes.get("picture"))
                    .provider("GOOGLE")
                    .build();
        }
        throw new OAuth2AuthenticationException("Provider non supporté: " + registrationId);
    }

    @Transactional
    public User processOAuth2User(OAuth2UserInfo userInfo) {
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("👤 Utilisateur existant: {}", user.getEmail());
        } else {
            user = registerNewUser(userInfo);
            log.info("🆕 Nouvel utilisateur inscrit: {}", user.getEmail());
        }
        
        return user;
    }

    private User registerNewUser(OAuth2UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setProvider(userInfo.getProvider());
        user.setProviderId(userInfo.getId());
        user.setName(userInfo.getName());
        user.setPictureUrl(userInfo.getPictureUrl());
        user.setRole(Role.CANDIDAT);
        user.setEnabled(true);
        user.setPassword(null);
        
        User savedUser = userRepository.save(user);
        
        // Créer un profil candidat
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(savedUser);
        profile.setNom(userInfo.getFamilyName() != null ? userInfo.getFamilyName() : "");
        profile.setPrenom(userInfo.getGivenName() != null ? userInfo.getGivenName() : userInfo.getName());
        
        candidateProfileRepository.save(profile);
        
        return savedUser;
    }
}