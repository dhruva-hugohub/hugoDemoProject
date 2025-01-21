package com.hugo.demo.service.impl;

import java.time.Instant;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserLoginResponseDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserRegisterResponseDTO;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.service.AuthService;
import com.hugo.demo.service.UserService;
import com.hugo.demo.user.UserEntity;
import com.hugo.demo.util.DateUtil;
import com.hugo.demo.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;


    public UserServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthService authService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    public UserLoginResponseDTO userLogin(UserLoginRequestDTO dto) {
        try {
            Optional<UserEntity> userEntity = userDAO.findByEmailAddress(dto.getEmail());

            if (userEntity.isPresent()) {
                    String token = authService.login(dto);
                    return mapToLoginResponse(userEntity.get(), token);
            } else {
                throw new IllegalArgumentException("Enter valid email");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid credentials");
        }
    }

    @Override
    public UserRegisterResponseDTO userRegister(UserRegisterRequestDTO dto) {
        try {
            if (userDAO.existsByEmailAddress(dto.getEmail())) {
                throw new IllegalArgumentException("Email is already registered");
            }
            String hashPin = hashingValue(dto.getPin());
            String hashPassword = hashingValue(dto.getPassword());
            String jwt = jwtUtil.generateToken(dto.getEmail());
            UserEntity userEntity =
                UserEntity.newBuilder().setName(dto.getName()).setEmail(dto.getEmail()).setPinHash(hashPin).setPhoneNumber(
                    dto.getPhonenumber()).setPasswordHash(hashPassword).setToken(jwt).setTokenExpiry(getNewTokenExpiry()).setCreatedAt(getCurrentTimeStamp())
                    .setUpdatedAt(getCurrentTimeStamp())
                    .build();
            UserEntity userEntityResponse = userDAO.save(userEntity);
            return mapToRegisterResponse(userEntityResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid credentials");
        }
    }


    private String hashingValue(String value){
        return passwordEncoder.encode(value);
    }

    private UserRegisterResponseDTO mapToRegisterResponse(UserEntity userEntity) {
        return UserRegisterResponseDTO.newBuilder().setUserId(userEntity.getUserId()).setEmail(userEntity.getEmail()).setName(userEntity.getName()).setPhoneNumber(
            userEntity.getPhoneNumber()).setToken(userEntity.getToken()).build();
    }

    private UserLoginResponseDTO mapToLoginResponse(UserEntity userEntity, String token) {
        return UserLoginResponseDTO.newBuilder().setUserId(userEntity.getUserId()).setEmail(userEntity.getEmail()).setName(userEntity.getName()).setPhoneNumber(
            userEntity.getPhoneNumber()).setToken(token).build();
    }

    private Timestamp getNewTokenExpiry() {
        return DateUtil.getTokenExpiry(jwtUtil.getExpirationTime());
    }

    private Timestamp getCurrentTimeStamp() {
        return Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).setNanos(Instant.now().getNano()).build();
    }
}
