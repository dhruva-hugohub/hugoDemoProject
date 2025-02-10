package com.hugo.demo.queues;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.hugo.demo.constants.ServerConstants;
import com.hugo.demo.exception.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class UserQueueService {

    private final SqsAsyncClient sqsAsyncClient;

    private final String queueUrl;


    @Autowired
    public UserQueueService(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.queueUrl = ServerConstants.QueueBaseUrl;
    }

    @Async
    public void sendUserDetailsToQueue(String messageBody, String queueEndpoint) {
        try {
            SendMessageRequest sendMsgRequest =
                SendMessageRequest.builder().queueUrl(queueUrl + queueEndpoint)
                    .messageBody(messageBody).build();
            sqsAsyncClient.sendMessage(sendMsgRequest);
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    public List<Message> pollUserDetailsFromQueue(String queueEndpoint) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl + queueEndpoint)
            .maxNumberOfMessages(5)
            .waitTimeSeconds(10)
            .build();

        CompletableFuture<ReceiveMessageResponse> receiveFuture = sqsAsyncClient.receiveMessage(receiveMessageRequest);

        return receiveFuture.join().messages();
    }

    public void deleteMessageFromQueue(String receiptHandle, String queueEndpoint) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl + queueEndpoint)
            .receiptHandle(receiptHandle)
            .build();

        sqsAsyncClient.deleteMessage(deleteMessageRequest);
    }
}
