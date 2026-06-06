// com.example.recrutement.auth.dto.OAuth2UserInfo.java
package com.example.recrutement.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String pictureUrl;
    private String provider;
}