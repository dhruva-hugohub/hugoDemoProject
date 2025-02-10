package com.hugo.demo.schedulingtasks;

import com.hugo.demo.service.CurrencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyValueScheduler {

    private final CurrencyService currencyService;

    public CurrencyValueScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void runTask() {
        currencyService.updateCurrencyRates();
    }
}
