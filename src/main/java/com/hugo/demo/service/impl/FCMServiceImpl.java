package com.hugo.demo.service.impl;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hugo.demo.notification.NotificationEntity;
import com.hugo.demo.service.FCMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Notification;


@Service
public class FCMServiceImpl implements FCMService {

    private final Logger logger = LoggerFactory.getLogger(FCMServiceImpl.class);

    @Override
    public void sendMessageToToken(NotificationEntity entity)
        throws InterruptedException, ExecutionException {

        Message message = getPreconfiguredMessageToToken(entity);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        logger.info("Sent message to token. Device token: {}, {} msg {}", entity.getToken(), response, jsonOutput);
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }


    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
            .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder()
                .setTag(topic).build()).build();
    }
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
            .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    private Message getPreconfiguredMessageToToken(NotificationEntity entity) {
        return getPreconfiguredMessageBuilder(entity).setToken(entity.getToken())
            .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(NotificationEntity entity) {
        AndroidConfig androidConfig = getAndroidConfig(entity.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(entity.getTopic());
        Notification notification = Notification.builder()
            .setTitle(entity.getTitle())
            .setBody(entity.getBody())
            .build();
        return Message.builder()
            .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(notification);
    }
}
