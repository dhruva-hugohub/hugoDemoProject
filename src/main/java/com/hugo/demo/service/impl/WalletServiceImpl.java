package com.hugo.demo.service.impl;

import java.util.Optional;

import com.hugo.demo.api.wallet.CreateWalletRequestDTO;
import com.hugo.demo.api.wallet.EditWalletRequestDTO;
import com.hugo.demo.api.wallet.WalletResponseDTO;
import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.facade.CurrencyFacade;
import com.hugo.demo.facade.WalletFacade;
import com.hugo.demo.service.WalletService;
import com.hugo.demo.util.ValidationUtil;
import com.hugo.demo.wallet.WalletEntity;
import org.springframework.stereotype.Service;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletFacade walletFacade;

    private final CurrencyFacade currencyFacade;

    public WalletServiceImpl(WalletFacade walletFacade, CurrencyFacade currencyFacade) {
        this.walletFacade = walletFacade;
        this.currencyFacade = currencyFacade;
    }

    @Override
    public WalletResponseDTO createWallet(CreateWalletRequestDTO createWalletRequestDTO) {
        try {
            ValidationUtil.validateWalletCreateRequest(createWalletRequestDTO);

            CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(createWalletRequestDTO.getCurrencyCode());
            double currencyValue = currencyEntity.getValue();

            WalletEntity walletEntity =
                WalletEntity.newBuilder().setUserId(createWalletRequestDTO.getUserId()).setWalletBalance(createWalletRequestDTO.getWalletBalance()/
                                                                                                         currencyValue)
                    .build();

            walletEntity = walletFacade.save(walletEntity);

            return WalletResponseDTO.newBuilder()
                .setWalletId(walletEntity.getWalletId())
                .setUserId(walletEntity.getUserId())
                .setWalletBalance(walletEntity.getWalletBalance() * currencyValue).build();
        }
        catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public WalletResponseDTO updateWallet(EditWalletRequestDTO editWalletRequestDTO) {
        try {
            ValidationUtil.validateEditWalletRequest(editWalletRequestDTO);

            CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(editWalletRequestDTO.getCurrencyCode());
            double currencyValue = currencyEntity.getValue();

            WalletEntity walletEntity = walletFacade.updateWallet(editWalletRequestDTO.getUserId(), editWalletRequestDTO.getWalletBalance()/currencyValue);

            return WalletResponseDTO.newBuilder()
                .setUserId(walletEntity.getUserId())
                .setWalletId(walletEntity.getWalletId())
                .setWalletBalance(walletEntity.getWalletBalance() * currencyValue).build();
        }
        catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    @Override
    public WalletResponseDTO findWalletID(long value, String field, String currencyCode) {
        try {
            Optional<WalletEntity> optionalWalletEntity;

            if ("userId".equals(field)) {
                optionalWalletEntity = walletFacade.findByUserId(value);
            } else if ("walletId".equals(field)) {
                optionalWalletEntity = walletFacade.findByWalletId(value);
            } else {
                throw new IllegalArgumentException("Invalid field: " + field);
            }

            WalletEntity walletEntity = optionalWalletEntity.orElseThrow(
                () -> new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provided credientials doesnt match with any wallet details")
            );

            CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(currencyCode);
            double currencyValue = currencyEntity.getValue();


            return WalletResponseDTO.newBuilder()
                .setUserId(walletEntity.getUserId())
                .setWalletId(walletEntity.getWalletId())
                .setWalletBalance(walletEntity.getWalletBalance()/currencyValue)
                .build();
        }
        catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }
}
