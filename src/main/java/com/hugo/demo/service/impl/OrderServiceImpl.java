package com.hugo.demo.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hugo.demo.api.order.CreateOrderRequestDTO;
import com.hugo.demo.api.order.EditOrderRequestDTO;
import com.hugo.demo.api.order.Order;
import com.hugo.demo.api.order.OrderResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.constants.ResourceConstants;
import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.enums.typeOfTransaction.TransactionType;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.GenericException;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
import com.hugo.demo.facade.CurrencyFacade;
import com.hugo.demo.facade.OrderFacade;
import com.hugo.demo.facade.ProductFacade;
import com.hugo.demo.facade.UserQuantityFacade;
import com.hugo.demo.facade.WalletFacade;
import com.hugo.demo.order.OrderEntity;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.queues.OrderQueueService;
import com.hugo.demo.service.OrderService;
import com.hugo.demo.userquantity.UserQuantityEntity;
import com.hugo.demo.util.ProtoJsonUtil;
import com.hugo.demo.util.ValidationUtil;
import com.hugo.demo.wallet.WalletEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderFacade orderFacade;

    private final WalletFacade walletFacade;

    private final ProductFacade productFacade;

    private final OrderQueueService orderQueueService;

    private final UserQuantityFacade userQuantityFacade;

    private final CurrencyFacade currencyFacade;


    @Autowired
    public OrderServiceImpl(OrderFacade orderFacade, WalletFacade walletFacade, ProductFacade productFacade,
                            OrderQueueService orderQueueService, UserQuantityFacade userQuantityFacade, CurrencyFacade currencyFacade) {
        this.orderFacade = orderFacade;
        this.walletFacade = walletFacade;
        this.productFacade = productFacade;
        this.orderQueueService = orderQueueService;
        this.userQuantityFacade = userQuantityFacade;
        this.currencyFacade = currencyFacade;
    }


    @Override
    @Transactional
    public PlainResponseDTO createOrder(CreateOrderRequestDTO orderRequestDTO) {
        try {
            // Validating UserId, ProviderId (Optional Check) , MetalId, TransactionType, Amount (Optional Check).
            ValidationUtil.validateCreateOrderRequest(orderRequestDTO);

            CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(orderRequestDTO.getCurrencyCode());
            double currencyValue = currencyEntity.getValue();

            // Set Order Entity Quantity if order type is SELL or Set Amount if order type is BUY.
            OrderEntity orderEntity;
            orderEntity = OrderEntity.newBuilder().setMetalId(orderRequestDTO.getMetalId())
                .setProviderId(orderRequestDTO.getProviderId())
                .setUserId(orderRequestDTO.getUserId())
                .setOrderStatus(ResourceConstants.PENDING)
                .setAmount(orderRequestDTO.getTransactionType() == TransactionType.BUY ? orderRequestDTO.getAmount() / currencyValue : 0.00)
                .setQuantity(orderRequestDTO.getTransactionType() == TransactionType.SELL ? orderRequestDTO.getQuantity() : 0.00)
                .setTypeOfTransaction(orderRequestDTO.getTransactionType())
                .build();


            // Create the Order
            OrderEntity orderEntityResponse = orderFacade.createOrder(orderEntity);
            // Send Order to SQS.
            orderQueueService.sendOrderToQueue(orderEntity);
            logger.info("Order created successfully with orderId: {}", orderEntityResponse.getOrderId());
            // Return Order ID.
            return PlainResponseDTO.newBuilder().setMessage(String.valueOf(orderEntityResponse.getOrderId())).build();
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(EditOrderRequestDTO orderRequestDTO) {

        try {
            ValidationUtil.validateUpdateOrderRequest(orderRequestDTO);

            boolean isOrderExists = orderFacade.checkOrderExistsByOrderId(orderRequestDTO.getOrderId());

            if (isOrderExists) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Order doesn't exist with orderId : " + orderRequestDTO.getOrderId());
            }

            OrderEntity orderEntity = OrderEntity.newBuilder().setOrderId(orderRequestDTO.getOrderId()).setOrderStatus(
                orderRequestDTO.getOrderStatus()).build();

            OrderEntity updatedOrderEntity = orderFacade.updateOrderStatus(orderEntity);

            CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(orderRequestDTO.getCurrencyCode());
            double currencyValue = currencyEntity.getValue();

            Order updatedOrderResponse =
                Order.newBuilder().setMetalId(updatedOrderEntity.getMetalId()).setProviderId(updatedOrderEntity.getProviderId())
                    .setUserId(updatedOrderEntity.getUserId()).setAmount(updatedOrderEntity.getAmount())
                    .setOrderStatus(updatedOrderEntity.getOrderStatus()).setClosingBalance(updatedOrderEntity.getClosingBalance())
                    .setAmount(updatedOrderEntity.getAmount()/ currencyValue).setTransactionType(updatedOrderEntity.getTypeOfTransaction())
                    .setQuantity(updatedOrderEntity.getQuantity()).build();

            return OrderResponseDTO.newBuilder().setOrder(updatedOrderResponse).build();
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public OrderResponseDTO getOrderDetailsByOrderId(long orderId, String currencyCode) {
        Optional<OrderEntity> orderEntity = orderFacade.getOrderDetailsByOrderId(orderId);

        OrderEntity updatedOrderEntity;

        CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(currencyCode);
        double currencyValue = currencyEntity.getValue();

        if (orderEntity.isPresent()) {
            updatedOrderEntity = orderEntity.get();
        } else {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provided orderId does not exist");
        }

        Order updatedOrderResponse = Order.newBuilder().setOrderId(updatedOrderEntity.getOrderId()).setMetalId(updatedOrderEntity.getMetalId())
            .setProviderId(updatedOrderEntity.getProviderId())
            .setUserId(updatedOrderEntity.getUserId()).setAmount(updatedOrderEntity.getAmount() * currencyValue).setOrderStatus(updatedOrderEntity.getOrderStatus())
            .setClosingBalance(updatedOrderEntity.getClosingBalance())
            .setTransactionType(updatedOrderEntity.getTypeOfTransaction()).setQuantity(updatedOrderEntity.getQuantity()).build();

        return OrderResponseDTO.newBuilder().setOrder(updatedOrderResponse).build();
    }

    @Override
    public PaginatedOrders fetchOrders(OrderFilter orderFilter) {
        try {
            ValidationUtil.validatePaginationInputs(orderFilter.getPage(), orderFilter.getPageSize());
            ValidationUtil.validateSortBy(orderFilter.getSortBy(), "orderId", "providerId", "userId", "metalId", "create_ts");

            return orderFacade.fetchOrders(orderFilter);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }


    @Override
    @Async
    @Transactional
//    @Scheduled(fixedDelay = 5000)
    public void processPendingOrders() {

            try {
                List<Message> processOrderList = orderQueueService.pollOrderDetailsFromQueue();
                for (Message message : processOrderList) {
                    processSingleOrder(message);
                }
            } catch (Exception e) {
                throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
            }
    }


    private void processSingleOrder(Message message) {
        try {
            OrderEntity orderEntity = ProtoJsonUtil.fromJson(message.body(), OrderEntity.class);
            assert orderEntity != null;

            Optional<WalletEntity> walletEntity = walletFacade.findByUserId(orderEntity.getUserId());
            Optional<UserQuantityEntity> userQuantityEntity = userQuantityFacade.findByMetalAndUserId(
                orderEntity.getUserId(), orderEntity.getMetalId());
            Optional<ProductEntity> productEntity = productFacade.fetchProductDetails(orderEntity.getProviderId(), orderEntity.getMetalId());

            double metalQuantity = orderEntity.getQuantity();
            double amount = orderEntity.getAmount();

            if (orderEntity.getTypeOfTransaction() == TransactionType.BUY) {
                assert productEntity.isPresent();
                amount = processBuyOrder(orderEntity, walletEntity, productEntity);
                metalQuantity = amount / productEntity.get().getProductValue();
            } else if (orderEntity.getTypeOfTransaction() == TransactionType.SELL) {
                amount = processSellOrder(orderEntity, userQuantityEntity, productEntity);
                metalQuantity *= -1;
            }

            finalizeOrderProcessing(orderEntity, userQuantityEntity, metalQuantity, amount, message);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred when processing order", e);
        }
    }

    private double processBuyOrder(OrderEntity orderEntity, Optional<WalletEntity> walletEntity,
                                   Optional<ProductEntity> productEntity) {
        validateBuyOrder(orderEntity, walletEntity, productEntity);

        double amount = orderEntity.getAmount();
        assert productEntity.isPresent();
        double metalQuantity = amount / productEntity.get().getProductValue();

        if (metalQuantity < 0.01) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Metal quantity is less than 0.01 is not allowed.");
        }

        return -1 * amount; // Deduct amount for BUY
    }

    private void validateBuyOrder(OrderEntity orderEntity, Optional<WalletEntity> walletEntity,
                                  Optional<ProductEntity> productEntity) {
        if (productEntity.isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Enter Valid Metal Id to Create Order");
        }

        if (walletEntity.isPresent() && walletEntity.get().getWalletBalance() < orderEntity.getAmount()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Wallet Balance is not enough");
        }
    }

    private double processSellOrder(OrderEntity orderEntity, Optional<UserQuantityEntity> userQuantityEntity,
                                    Optional<ProductEntity> productEntity) {
        validateSellOrder(orderEntity, userQuantityEntity, productEntity);

        assert productEntity.isPresent();
        double amount = orderEntity.getQuantity() * productEntity.get().getProductValue();
        if (amount < 0.1) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Amount is less than 0.1 is not allowed.");
        }

        return amount;
    }

    private void validateSellOrder(OrderEntity orderEntity, Optional<UserQuantityEntity> userQuantityEntity,
                                   Optional<ProductEntity> productEntity) {
        if (userQuantityEntity.isPresent() && userQuantityEntity.get().getQuantity() >= orderEntity.getQuantity()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "User doesn't have enough quantity which they want to sell.");
        }

        if (productEntity.isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Enter Valid Metal Id to Create Order");
        }
    }

    private void finalizeOrderProcessing(OrderEntity orderEntity, Optional<UserQuantityEntity> userQuantityEntity,
                                         double metalQuantity, double amount, Message message) {
        try {
            String userQuantityJson = fetchUserQuantitiesJson(userQuantityEntity);

            WalletEntity updatedWalletEntity = walletFacade.updateWallet(orderEntity.getUserId(), -1 * amount);
            updateProductStock(orderEntity, metalQuantity);
            updateUserQuantity(orderEntity, metalQuantity);

            OrderEntity updatedOrderEntity = buildUpdatedOrder(orderEntity, metalQuantity, amount,
                userQuantityJson, updatedWalletEntity.getWalletBalance());

            orderFacade.updateOrderStatus(updatedOrderEntity);
            orderQueueService.deleteMessageFromQueue(message.receiptHandle());
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred when finalizing order processing", e);
        }
    }

    private String fetchUserQuantitiesJson(Optional<UserQuantityEntity> userQuantityEntity) {
        return userQuantityEntity.map(entity -> {
            List<UserQuantityEntity> userQuantityEntityList = userQuantityFacade.fetchQuantitesByUserId(entity.getUserId());
            return convertListToJson(userQuantityEntityList);
        }).orElse("");
    }

    private void updateProductStock(OrderEntity orderEntity, double metalQuantity) {
        productFacade.updateStock(orderEntity.getProviderId(), orderEntity.getMetalId(),
            (-1 * (int) metalQuantity));
    }

    private void updateUserQuantity(OrderEntity orderEntity, double metalQuantity) {
        UserQuantityEntity updatedUserQuantityEntity = UserQuantityEntity.newBuilder()
            .setMetalId(orderEntity.getMetalId())
            .setUserId(orderEntity.getUserId())
            .setQuantity(metalQuantity)
            .build();
        userQuantityFacade.updateQuantity(updatedUserQuantityEntity);
    }

    private OrderEntity buildUpdatedOrder(OrderEntity orderEntity, double metalQuantity, double amount,
                                          String userQuantityJson, double closingBalance) {
        return OrderEntity.newBuilder()
            .setOrderId(orderEntity.getOrderId())
            .setMetalId(orderEntity.getMetalId())
            .setProviderId(orderEntity.getProviderId())
            .setUserId(orderEntity.getUserId())
            .setQuantity(metalQuantity)
            .setItemsQuantity(userQuantityJson)
            .setOrderStatus(ResourceConstants.SUCCESS)
            .setTypeOfTransaction(orderEntity.getTypeOfTransaction())
            .setClosingBalance(closingBalance)
            .setAmount(amount)
            .build();
    }

    public String convertListToJson(List<UserQuantityEntity> entityList) {
        return entityList.stream()
            .map(entity -> "\"" + entity.getMetalId() + "\" : \"" + entity.getQuantity() + "\"")
            .collect(Collectors.joining(", ", "{", "}"));
    }

}
