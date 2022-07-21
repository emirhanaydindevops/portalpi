package com.canovate.uploader.security;

public interface ITokenManager {

    void checkToken();

    String getBearerToken();
}
