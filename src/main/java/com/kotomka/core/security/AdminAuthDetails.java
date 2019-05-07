package com.kotomka.core.security;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AdminAuthDetails implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest httpServletRequest) {
        return new CustomWebAuthenticationDetails(httpServletRequest);
    }

    public static class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

        public CustomWebAuthenticationDetails(HttpServletRequest request) {
            super(request);
        }
    }
}