package com.hugo.demo.facade;

import java.util.Optional;

import com.hugo.demo.dao.WalletDAO;
import com.hugo.demo.wallet.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletFacade {

    private final WalletDAO walletDAO;

    public WalletFacade(WalletDAO walletDAO) {
        this.walletDAO = walletDAO;
    }

   public Optional<WalletEntity> findByWalletId(long walletId){
        return walletDAO.findByWalletId(walletId);
   }

    public Optional<WalletEntity> findByUserId(long userId){
        return walletDAO.findByUserId(userId);
    }

   public boolean checkExistsByUser(String field, String value){
        return walletDAO.checkExistsByUser(field, value);
   }

   public WalletEntity save(WalletEntity walletEntity){
        return walletDAO.save(walletEntity);
   }

   public WalletEntity updateWallet(long userId, double walletBalance){
        return walletDAO.updateWallet(userId, walletBalance);
   }
}
