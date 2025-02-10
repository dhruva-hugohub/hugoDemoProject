package com.hugo.demo.dao;

import java.util.Optional;

import com.hugo.demo.wallet.WalletEntity;

public interface WalletDAO {

    Optional<WalletEntity> findByWalletId(long walletId);

    Optional<WalletEntity> findByUserId(long userId);

    boolean checkExistsByUser(String field, String value);

    WalletEntity save(WalletEntity walletEntity);

    WalletEntity updateWallet(long userId, double walletBalance);

}
