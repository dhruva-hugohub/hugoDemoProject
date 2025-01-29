package com.hugo.demo.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.constants.RegexConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;

public class ValidationUtil {

    public static void validateUserRegisterRequest(UserRegisterRequestDTO dto) {
        if (dto == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }

        validateField(dto.getName(), "name", "Invalid name format");
        validateField(dto.getEmail(), "email", "Invalid email format");
        validateField(dto.getPhoneNumber(), "phoneNumber", "Invalid phone number format");
        validateField(dto.getPassword(), "password", "Invalid password format");
        validateField(dto.getPin(), "pin", "Invalid pin format");
    }

    public static void validateUserLoginRequest(UserLoginRequestDTO dto) {
        if (dto == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }
    }

    private static void validateField(String requestObject, String objectType, String errorMessage) {
        if (requestObject == null || objectType == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, errorMessage);
        }

        Pattern pattern = switch (objectType) {
            case "email" -> RegexConstants.emailPattern;
            case "phoneNumber" -> RegexConstants.phoneNumberPattern;
            case "password" -> RegexConstants.passwordPattern;
            case "pin" -> RegexConstants.pinPattern;
            default -> RegexConstants.namePattern;
        };

        Matcher matcher = pattern.matcher(requestObject);
        if (!matcher.matches()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, errorMessage);
        }
    }
}

