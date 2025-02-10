package com.hugo.demo.queues;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.hugo.demo.constants.ServerConstants;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.order.OrderEntity;
import com.hugo.demo.util.ProtoJsonUtil;
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
public class RefundQueueService {
    private final SqsAsyncClient sqsAsyncClient;

    private final String queueUrl;

    @Autowired
    public RefundQueueService(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.queueUrl = ServerConstants.QueueBaseUrl + ServerConstants.refundQueue;
    }

    @Async
    public void sendRefundToQueue(OrderEntity orderEntity) {
        try {
            SendMessageRequest sendMsgRequest =
                SendMessageRequest.builder().queueUrl(queueUrl)
                    .messageBody(ProtoJsonUtil.toJson(orderEntity)).build();
            sqsAsyncClient.sendMessage(sendMsgRequest);
        } catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    public List<Message> pollRefundDetailsFromQueue() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(5)
            .waitTimeSeconds(10)
            .build();

        CompletableFuture<ReceiveMessageResponse> receiveFuture = sqsAsyncClient.receiveMessage(receiveMessageRequest);

        return receiveFuture.join().messages();
    }

    public void deleteMessageFromQueue(String receiptHandle) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build();

        sqsAsyncClient.deleteMessage(deleteMessageRequest);
    }
}
