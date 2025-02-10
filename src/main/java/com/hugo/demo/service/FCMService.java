package com.hugo.demo.service;

import java.util.concurrent.ExecutionException;

import com.hugo.demo.notification.NotificationEntity;

public interface FCMService {

    void sendMessageToToken(NotificationEntity entity)
        throws InterruptedException, ExecutionException;
}
