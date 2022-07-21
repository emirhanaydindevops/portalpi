package com.canovate.uploader.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expires_in")
    Long expiresIn;

    @JsonProperty("refresh_token")
    String refreshToken;

    @JsonProperty("refresh_expires_in")
    Long refreshExpiresIn;

    @JsonProperty("scope")
    String scope;

    @JsonProperty("session_state")
    String sessionState;

    LocalDateTime expiresAt;

    LocalDateTime refreshExpiresAt;

    public void calculateExpires() {
        expiresAt = LocalDateTime.now().plusSeconds(expiresIn);
        refreshExpiresAt = LocalDateTime.now().plusSeconds(refreshExpiresIn);
    }

}
