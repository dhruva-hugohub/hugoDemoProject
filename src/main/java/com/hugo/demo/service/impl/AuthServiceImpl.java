package com.hugo.demo.service.impl;

import com.hugo.demo.config.JwtTokenProvider;
import com.hugo.demo.service.AuthService;
import com.hugo.demo.util.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String login(String email, String password, HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            email,
            password
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String clientIpAddress = CommonUtil.getClientIp(request);

        return jwtTokenProvider.generateToken(authentication, clientIpAddress);
    }


}
