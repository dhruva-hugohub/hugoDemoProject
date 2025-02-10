package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.user.UserVerifyPinRequestDTO;
import com.hugo.demo.api.wallet.CreateWalletRequestDTO;
import com.hugo.demo.api.wallet.EditWalletRequestDTO;
import com.hugo.demo.api.wallet.WalletResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.WalletService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_WALLET_PATH)
@Tag(name = "Wallet APIs", description = "Wallet APIs.")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create-wallet")
    public ApiResponse verifyPin(@RequestBody CreateWalletRequestDTO createWalletRequestDTO) {
        WalletResponseDTO walletResponseDTO = walletService.createWallet(createWalletRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, walletResponseDTO);
    }

    @PutMapping("/update-wallet")
    public ApiResponse verifyPin(@RequestBody EditWalletRequestDTO editWalletRequestDTO) {
        WalletResponseDTO walletResponseDTO = walletService.updateWallet(editWalletRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, walletResponseDTO);
    }

    @GetMapping("/wallet-details")
    public ApiResponse walletDetails(@RequestParam Long userId) {
        WalletResponseDTO walletResponseDTO = walletService.findWalletID(userId,"userId");
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, walletResponseDTO);
    }
}
