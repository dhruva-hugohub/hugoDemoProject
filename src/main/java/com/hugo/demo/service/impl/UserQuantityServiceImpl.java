package com.hugo.demo.service.impl;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.api.userquantity.CreateUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.EditUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.UserQuantityResponseDTO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.facade.UserQuantityFacade;
import com.hugo.demo.service.UserQuantityService;
import com.hugo.demo.userquantity.UserQuantityEntity;
import com.hugo.demo.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserQuantityServiceImpl implements UserQuantityService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserQuantityServiceImpl.class);
    private final UserQuantityFacade userQuantityFacade;

    public UserQuantityServiceImpl(UserQuantityFacade userQuantityFacade) {
        this.userQuantityFacade = userQuantityFacade;
    }

    @Override
    public UserQuantityResponseDTO createUserQuantity(CreateUserQuantityRequestDTO createUserQuantityRequestDTO) {
        try {
            ValidationUtil.validateCreateUserQuantity(createUserQuantityRequestDTO);

            UserQuantityEntity userQuantityEntity = UserQuantityEntity.newBuilder().setUserId(createUserQuantityRequestDTO.getUserId()).setMetalId(
                createUserQuantityRequestDTO.getMetalId()).setQuantity(createUserQuantityRequestDTO.getQuantity()).build();

            userQuantityEntity = userQuantityFacade.save(userQuantityEntity);
            return UserQuantityResponseDTO.newBuilder()
                .setUserId(userQuantityEntity.getUserId())
                .setQuantity(userQuantityEntity.getQuantity())
                .setMetalId(
                    userQuantityEntity.getMetalId()).build();
        } catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public UserQuantityResponseDTO updateUserMetalQuantity(EditUserQuantityRequestDTO editUserQuantityRequestDTO){
        try {
            ValidationUtil.validateUpdateUserQuantity(editUserQuantityRequestDTO);

            UserQuantityEntity userQuantityEntity = UserQuantityEntity.newBuilder().setUserId(editUserQuantityRequestDTO.getUserId()).setMetalId(
                editUserQuantityRequestDTO.getMetalId()).setQuantity(editUserQuantityRequestDTO.getQuantity()).build();

            userQuantityEntity = userQuantityFacade.updateQuantity(userQuantityEntity);
            return UserQuantityResponseDTO.newBuilder()
                .setUserId(userQuantityEntity.getUserId())
                .setQuantity(userQuantityEntity.getQuantity())
                .setMetalId(
                    userQuantityEntity.getMetalId()).build();
        } catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public void fetchUserQuantitesByUserId(long userId){
        try {
            List<UserQuantityEntity> userQuantityEntityList = userQuantityFacade.fetchQuantitesByUserId(userId);
            LOGGER.info("fetchUserQuantitiesByUserId:{}", userQuantityEntityList);
        }
        catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public UserQuantityResponseDTO fetchUserQuantitesByMetalAndUserId(long userId, String metalId){
        try{

            Optional<UserQuantityEntity> userMetalQuantity = userQuantityFacade.findByMetalAndUserId(userId, metalId);
            if(userMetalQuantity.isPresent()){
               return UserQuantityResponseDTO.newBuilder().setMetalId(userMetalQuantity.get().getMetalId()).setUserId(userMetalQuantity.get().getUserId()).setQuantity(userMetalQuantity.get().getQuantity()).setQuantity(userMetalQuantity.get().getQuantity()).build();
            }
            else{
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provided metal id is null");
            }
        }
        catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }
}
