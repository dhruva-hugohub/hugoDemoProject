package com.hugo.demo.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    String login(String email, String password, HttpServletRequest request);

}
