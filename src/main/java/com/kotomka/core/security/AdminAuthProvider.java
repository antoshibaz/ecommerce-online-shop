package com.kotomka.core.security;

import com.kotomka.core.exceptions.AuthException;
import com.kotomka.core.model.server.UserModel;
import com.kotomka.core.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

@Component
public class AdminAuthProvider implements AuthenticationProvider {

    private final AuthService authService;

    @Autowired
    public AdminAuthProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserModel userModel;
        try {
            userModel = authService.checkSystemUser(username, DigestUtils.md5DigestAsHex(password.getBytes("utf-8")).toUpperCase());
        } catch (UnsupportedEncodingException e) {
            throw new AuthException("");
        }
        return new UsernamePasswordAuthenticationToken(userModel, password, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}