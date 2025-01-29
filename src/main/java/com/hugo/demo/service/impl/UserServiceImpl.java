package com.hugo.demo.service.impl;

import java.time.Instant;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserRegisterResponseDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.service.AuthService;
import com.hugo.demo.service.UserService;
import com.hugo.demo.user.UserEntity;
import com.hugo.demo.util.ValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    public UserServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, AuthService authService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Override
    public UserResponseDTO userLogin(UserLoginRequestDTO dto) {
        try {
            ValidationUtil.validateUserLoginRequest(dto);

            Optional<UserEntity> userEntity = userDAO.findByEmailAddress(dto.getEmail());

            if (userEntity.isPresent()) {
                String token = authService.login(dto.getEmail(), dto.getPassword());
                return mapToResponse(userEntity.get(), token);
            } else {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Entered emailAddress is not present : " + dto.getEmail());
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }

    }

    @Override
    public UserResponseDTO userRegister(UserRegisterRequestDTO dto) {
        try {
            ValidationUtil.validateUserRegisterRequest(dto);

            Optional<UserEntity> existUserEntity = userDAO.findByEmailAddress(dto.getEmail());

            if (existUserEntity.isPresent()) {
                throw new RecordAlreadyExistsException(CommonStatusCode.FAILED, "Account already exists with : " + dto.getEmail());
            }

            String hashPin = hashingValue(dto.getPin());
            String hashPassword = hashingValue(dto.getPassword());
            UserEntity userEntity =
                UserEntity.newBuilder().setName(dto.getName()).setEmail(dto.getEmail()).setPinHash(hashPin).setPhoneNumber(dto.getPhoneNumber())
                    .setPasswordHash(hashPassword).setCreatedAt(getCurrentTimeStamp()).setUpdatedAt(getCurrentTimeStamp()).build();

            UserEntity userEntityResponse = userDAO.save(userEntity);

            String token = authService.login(dto.getEmail(), dto.getPassword());

            return mapToResponse(userEntityResponse, token);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        }
    }


    private String hashingValue(String value) {
        return passwordEncoder.encode(value);
    }


    private UserResponseDTO mapToResponse(UserEntity userEntity, String token) {
        return UserResponseDTO.newBuilder().setUserId(userEntity.getUserId()).setEmail(userEntity.getEmail()).setName(userEntity.getName())
            .setPhoneNumber(userEntity.getPhoneNumber()).setToken(token).build();
    }

    private Timestamp getCurrentTimeStamp() {
        return Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).setNanos(Instant.now().getNano()).build();
    }


}
