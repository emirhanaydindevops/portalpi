package com.canovate.uploader.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class TokenManagerProcessor implements ITokenManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${auth.client}")
    private String client;

    @Value("${auth.client-secret}")
    private String clientSecret;

    @Value("${auth.token-url}")
    private String tokenUrl;

    RestTemplate restTemplate = new RestTemplate();

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Optional<Token> token = Optional.ofNullable(null);


    @Override
    public void checkToken() {
        token.ifPresentOrElse(token1 -> refreshTokenIfNecessary(), () -> getToken());
    }

    @Override
    public String getBearerToken() {
        return "Bearer " + token.orElse(new Token()).accessToken;
    }

    private void getToken() {
        try {
            log.trace("Getting access token");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(client, clientSecret);


            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Token> responseEntity = restTemplate.postForEntity(tokenUrl, request, Token.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                setToken(responseEntity.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setToken(@NotNull Token token) {
        token.calculateExpires();
        this.token = Optional.of(token);
    }

    private void refreshToken() {
        try {
            log.trace("Refreshing access token");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(client, clientSecret);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", token.get().refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Token> responseEntity = restTemplate.postForEntity(tokenUrl, request, Token.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                setToken(responseEntity.getBody());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshTokenExpired() {
        token = Optional.ofNullable(null);
        getToken();
    }

    private void refreshTokenIfNecessary() {
        token.ifPresentOrElse(token1 -> {
            if (token1.refreshExpiresAt.isBefore(LocalDateTime.now().plusMinutes(1))) {
                refreshTokenExpired();
            } else if (token1.expiresAt.isBefore((LocalDateTime.now().plusMinutes(1)))) {
                refreshToken();
            }
        }, () -> getToken());
    }
}
