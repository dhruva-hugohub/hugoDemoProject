package com.hugo.demo.service;

import com.hugo.demo.api.userquantity.CreateUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.EditUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.UserQuantityResponseDTO;

public interface UserQuantityService {

    UserQuantityResponseDTO createUserQuantity(CreateUserQuantityRequestDTO createUserQuantityRequestDTO);

    UserQuantityResponseDTO updateUserMetalQuantity(EditUserQuantityRequestDTO editUserQuantityRequestDTO);

    void fetchUserQuantitesByUserId(long userId);

    UserQuantityResponseDTO fetchUserQuantitesByMetalAndUserId(long userId, String metalId);
}
