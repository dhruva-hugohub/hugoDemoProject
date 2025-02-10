package com.hugo.demo.service.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hugo.demo.alert.AlertEntity;
import com.hugo.demo.api.alert.FcmUpdateProto;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.user.EditUserPasswordRequestDTO;
import com.hugo.demo.api.user.EditUserPinRequestDTO;
import com.hugo.demo.api.user.EditUserRequestDTO;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.api.user.UserVerifyPinRequestDTO;
import com.hugo.demo.constants.ServerConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
import com.hugo.demo.facade.AlertFacade;
import com.hugo.demo.facade.ProductFacade;
import com.hugo.demo.facade.UserFacade;
import com.hugo.demo.facade.UserQuantityFacade;
import com.hugo.demo.facade.WalletFacade;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.queues.UserQueueService;
import com.hugo.demo.service.AuthService;
import com.hugo.demo.service.TokenBlackList;
import com.hugo.demo.service.UserService;
import com.hugo.demo.user.UserEntity;
import com.hugo.demo.userquantity.UserQuantityEntity;
import com.hugo.demo.util.ProtoJsonUtil;
import com.hugo.demo.util.ValidationUtil;
import com.hugo.demo.wallet.WalletEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class UserServiceImpl implements UserService {
    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final UserQuantityFacade userQuantityFacade;
    private final ProductFacade productFacade;
    private final WalletFacade walletFacade;
    private final TokenBlackList tokenBlackList;
    private final UserQueueService userQueueService;
    private final AlertFacade alertFacade;


    @Autowired
    public UserServiceImpl(UserFacade userFacade, PasswordEncoder passwordEncoder, AuthService authService, UserQuantityFacade userQuantityFacade,
                           ProductFacade productFacade, WalletFacade walletFacade, TokenBlackList tokenBlackList, UserQueueService userQueueService,
                           AlertFacade alertFacade) {
        this.userFacade = userFacade;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.userQuantityFacade = userQuantityFacade;
        this.productFacade = productFacade;
        this.walletFacade = walletFacade;
        this.tokenBlackList = tokenBlackList;
        this.userQueueService = userQueueService;
        this.alertFacade = alertFacade;
    }

    @Override
    public UserResponseDTO userLogin(UserLoginRequestDTO dto, HttpServletRequest request) {
        try {
            ValidationUtil.validateUserLoginRequest(dto);

            Optional<UserEntity> userEntity = userFacade.findByEmailAddress(dto.getEmail());

            if (userEntity.isPresent()) {
                String token = authService.login(dto.getEmail(), dto.getPassword(), request);
                FcmUpdateProto fcmUpdateProto =
                    FcmUpdateProto.newBuilder().setFcmToken(dto.getFcmToken()).setUserId(userEntity.get().getUserId()).build();
                userQueueService.sendUserDetailsToQueue(ProtoJsonUtil.toJson(fcmUpdateProto), ServerConstants.LoginQueue);
                return mapToResponse(userEntity.get(), token);
            } else {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Entered emailAddress is not present : " + dto.getEmail());
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public UserResponseDTO userRegister(UserRegisterRequestDTO dto, HttpServletRequest request) {
        try {
            ValidationUtil.validateUserRegisterRequest(dto);

            boolean existUsers = userFacade.checkExistsByUser("emailAddress", dto.getEmail());

            if (existUsers) {
                throw new RecordAlreadyExistsException(CommonStatusCode.FAILED, "Account already exists with : " + dto.getEmail());
            }

            String hashPin = hashingValue(dto.getPin());
            String hashPassword = hashingValue(dto.getPassword());
            UserEntity userEntity =
                UserEntity.newBuilder().setName(dto.getName()).setEmail(dto.getEmail()).setPinHash(hashPin).setPhoneNumber(dto.getPhoneNumber())
                    .setPasswordHash(hashPassword).build();

            UserEntity userEntityResponse = userFacade.save(userEntity);

            userQueueService.sendUserDetailsToQueue(ProtoJsonUtil.toJson(userEntityResponse), ServerConstants.UserRegisterQueue);

            String token = authService.login(dto.getEmail(), dto.getPassword(), request);

            return mapToResponse(userEntityResponse, token);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
//    @Scheduled(fixedDelay = 5000)
    public void processUserRegistration() {
        try {
            List<Message> processUserList = userQueueService.pollUserDetailsFromQueue(ServerConstants.UserRegisterQueue);
            for (Message message : processUserList) {
                UserEntity userEntity = ProtoJsonUtil.fromJson(message.body(), UserEntity.class);
                List<ProductEntity> productEntityList = productFacade.fetchAllProducts();
                Set<String> metalSet = new HashSet<>();
                for (ProductEntity productEntity : productEntityList) {
                    metalSet.add(productEntity.getMetalId());
                }

                for (String metalId : metalSet) {
                    assert userEntity != null;
                    UserQuantityEntity userQuantityEntity =
                        UserQuantityEntity.newBuilder().setUserId(userEntity.getUserId()).setMetalId(metalId).setQuantity(0.00).build();
                    userQuantityFacade.save(userQuantityEntity);
                }

                assert userEntity != null;
                WalletEntity walletEntity = WalletEntity.newBuilder().setUserId(userEntity.getUserId()).setWalletBalance(0.00).build();
                walletFacade.save(walletEntity);
                userQueueService.deleteMessageFromQueue(message.receiptHandle(), ServerConstants.UserRegisterQueue);
            }

        } catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
//    @Scheduled(fixedDelay = 5000)
    public void processUserLogin() {
        try {
            List<Message> processUserList = userQueueService.pollUserDetailsFromQueue(ServerConstants.LoginQueue);
            for (Message message : processUserList) {
                FcmUpdateProto fcmUpdateProto = ProtoJsonUtil.fromJson(message.body(), FcmUpdateProto.class);
                assert fcmUpdateProto != null;
                List<AlertEntity> alertEntityList = alertFacade.getAlertByUserId(fcmUpdateProto.getUserId());

                for (AlertEntity alertEntity : alertEntityList) {
                    AlertEntity updatedAlertEntity = AlertEntity.newBuilder().setProviderId(alertEntity.getProviderId()).setUserId(
                        alertEntity.getUserId()).setMetalId(alertEntity.getMetalId()).setFcmToken(fcmUpdateProto.getFcmToken()).build();
                    alertFacade.editItemRecord(updatedAlertEntity);
                }

                userQueueService.deleteMessageFromQueue(message.receiptHandle(), ServerConstants.LoginQueue);
            }

        } catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }


    @Override
    public PlainResponseDTO verifyPin(UserVerifyPinRequestDTO userVerifyPinRequestDTO) {
        try {

            ValidationUtil.validateVerifyPinRequest(userVerifyPinRequestDTO);

            Optional<UserEntity> userEntity = userFacade.findByUserId(userVerifyPinRequestDTO.getUserId());

            if (userEntity.isPresent()) {
                boolean verified = passwordEncoder.matches(userVerifyPinRequestDTO.getPin(), userEntity.get().getPinHash());

                if (verified) {
                    return PlainResponseDTO.newBuilder().setMessage("Pin Verified Successfully").build();
                } else {
                    return PlainResponseDTO.newBuilder().setMessage("Enter Correct Pin").build();
                }

            } else {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                    "Entered User is not present : " + userVerifyPinRequestDTO.getUserId());
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public PlainResponseDTO logout(HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            String token = "";
            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }

            boolean isLogout = tokenBlackList.addToBlacklist(token);

            if (isLogout) {
                return PlainResponseDTO.newBuilder().setMessage("User Logged out Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't logout user. Please try again.").build();
            }

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public UserResponseDTO getUserDetails(long userId) {
        try {

            Optional<UserEntity> userEntity = userFacade.findByUserId(userId);

            if (userEntity.isPresent()) {
                return mapToResponse(userEntity.get(), "");
            } else {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Entered User is not present : " + userId);
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }

    }

    @Override
    public UserResponseDTO editUserDetails(EditUserRequestDTO dto) {
        try {
            ValidationUtil.validateUserEditRequest(dto);

            boolean existUsers = userFacade.checkExistsByUser("userId", String.valueOf(dto.getUserId()));

            if (!existUsers) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Account doesn't exist with userId : " + dto.getUserId());
            }

            UserEntity userEntity =
                UserEntity.newBuilder().setUserId(dto.getUserId()).setName(dto.getName()).setPhoneNumber(dto.getPhoneNumber()).build();

            UserEntity userEntityResponse = userFacade.updateUser(userEntity);

            return mapToResponse(userEntityResponse, "");
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
    }

    @Override
    public PlainResponseDTO editUserPassword(EditUserPasswordRequestDTO dto) {
        try {
            ValidationUtil.validateEditPasswordRequest(dto);

            boolean existUsers = userFacade.checkExistsByUser("emailAddress", dto.getEmail());

            if (!existUsers) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Account doesn't exist with userId : " + dto.getEmail());
            }

            String hashPassword = hashingValue(dto.getNewPassword());

            UserEntity userEntity =
                UserEntity.newBuilder().setEmail(dto.getEmail()).setPasswordHash(hashPassword).build();

            boolean updateUserCredentials = userFacade.updateUserCredentials(userEntity);

            if (updateUserCredentials) {
                return PlainResponseDTO.newBuilder().setMessage("User Password Updated Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't Update User Password").build();
            }

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
    }

    @Override
    public PlainResponseDTO editUserPin(EditUserPinRequestDTO dto) {
        try {
            ValidationUtil.validateEditPinRequest(dto);

            boolean existUsers = userFacade.checkExistsByUser("emailAddress", dto.getEmail());

            if (!existUsers) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Account doesn't exist with userId : " + dto.getEmail());
            }

            String hashPin = hashingValue(dto.getNewPin());

            UserEntity userEntity =
                UserEntity.newBuilder().setEmail(dto.getEmail()).setPinHash(hashPin).build();

            boolean updateUserCredentials = userFacade.updateUserCredentials(userEntity);

            if (updateUserCredentials) {
                return PlainResponseDTO.newBuilder().setMessage("User Pin Updated Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't Update User Pin").build();
            }

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
    }


    @Override
    public PlainResponseDTO deleteUser(long userId) {
        try {

            boolean existUsers = userFacade.checkExistsByUser("userId", String.valueOf(userId));

            if (!existUsers) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Account doesn't exist with userId : " + userId);
            }

            boolean deleteUsers = userFacade.deleteUser(userId);

            if (deleteUsers) {
                return PlainResponseDTO.newBuilder().setMessage("User Deleted Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't Delete User").build();
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
    }


    private String hashingValue(String value) {
        return passwordEncoder.encode(value);
    }


    private UserResponseDTO mapToResponse(UserEntity userEntity, String token) {
        return UserResponseDTO.newBuilder().setUserId(userEntity.getUserId()).setEmail(userEntity.getEmail()).setName(userEntity.getName())
            .setProfileImage(userEntity.getProfileImage())
            .setPhoneNumber(userEntity.getPhoneNumber()).setToken(token).build();
    }

}
