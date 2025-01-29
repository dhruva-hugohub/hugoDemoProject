package com.hugo.demo.schedulingtasks;

import com.hugo.demo.service.LiveItemPriceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LiveItemPriceScheduler {

    private final LiveItemPriceService liveItemPriceService;

    public LiveItemPriceScheduler(LiveItemPriceService liveItemPriceService) {
        this.liveItemPriceService = liveItemPriceService;
    }

    @Scheduled(fixedRate = 1000*60*5)
    public void runTask() {
        liveItemPriceService.saveItemPricesForAllProviders( "USD", "g");
    }
}
