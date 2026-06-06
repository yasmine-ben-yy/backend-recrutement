// com.example.recrutement.auth.handler.OAuth2AuthenticationSuccessHandler.java
package com.example.recrutement.auth.handler;

import com.example.recrutement.auth.dto.OAuth2UserInfo;
import com.example.recrutement.auth.service.CustomOAuth2UserService;
import com.example.recrutement.security.JwtService;
import com.example.recrutement.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CustomOAuth2UserService customOAuth2UserService;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extraire les infos utilisateur
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        String pictureUrl = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");
        
        log.info("🎉 OAuth2 login réussi pour: {}", email);
        
        // Créer ou récupérer l'utilisateur
        OAuth2UserInfo userInfo = OAuth2UserInfo.builder()
                .id(providerId)
                .email(email)
                .name(name)
                .givenName(givenName)
                .familyName(familyName)
                .pictureUrl(pictureUrl)
                .provider("GOOGLE")
                .build();
        
        User user = customOAuth2UserService.processOAuth2User(userInfo);
        
        // Générer le JWT
        String jwtToken = jwtService.generateToken(user);
        String role = "ROLE_" + user.getRole().name();
        
        log.info("✅ JWT généré pour: {} - Rôle: {}", email, role);
        
        // ✅ Construire l'URL de redirection avec les paramètres
        String redirectUrl = frontendUrl + "/oauth2/redirect?" +
                "token=" + URLEncoder.encode(jwtToken, StandardCharsets.UTF_8) +
                "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
                "&role=" + URLEncoder.encode(role, StandardCharsets.UTF_8) +
                "&name=" + (name != null ? URLEncoder.encode(name, StandardCharsets.UTF_8) : "");
        
        log.info("🔀 Redirection vers: {}", redirectUrl);
        
        response.sendRedirect(redirectUrl);
    }
}