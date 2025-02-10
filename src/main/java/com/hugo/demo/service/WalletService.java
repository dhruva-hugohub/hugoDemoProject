package com.hugo.demo.service;

import com.hugo.demo.api.wallet.CreateWalletRequestDTO;
import com.hugo.demo.api.wallet.EditWalletRequestDTO;
import com.hugo.demo.api.wallet.WalletResponseDTO;

public interface WalletService {

    WalletResponseDTO createWallet(CreateWalletRequestDTO createWalletRequestDTO);

    WalletResponseDTO updateWallet(EditWalletRequestDTO editWalletRequestDTO);

    WalletResponseDTO findWalletID(long value, String field, String currencyCode);
}
