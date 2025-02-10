package com.hugo.demo.controller;

import java.time.LocalDateTime;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.order.CreateOrderRequestDTO;
import com.hugo.demo.api.order.EditOrderRequestDTO;
import com.hugo.demo.api.order.OrderResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.enums.typeOfTransaction.TransactionType;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;
import com.hugo.demo.service.OrderService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_ORDER_PATH)
@Tag(name = "Order APIs", description = "Order Management APIs.")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "/add")
    public ApiResponse createOrder(@RequestBody CreateOrderRequestDTO createOrderRequestDTO) {
        PlainResponseDTO response = orderService.createOrder(createOrderRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @GetMapping("/{orderId}")
    public ApiResponse getOrderDetails(@PathVariable int orderId) {
        OrderResponseDTO orderResponseDTO = orderService.getOrderDetailsByOrderId(orderId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, orderResponseDTO);
    }

    @PutMapping("/edit")
    public ApiResponse updateOrder(@RequestBody EditOrderRequestDTO editOrderRequestDTO) {
        OrderResponseDTO orderResponseDTO = orderService.updateOrderStatus(editOrderRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, orderResponseDTO);
    }

    @GetMapping("/orders")
    public ApiResponse getOrders(
        @RequestParam(required = false, defaultValue = "0") Long userId,
        @RequestParam(required = false, defaultValue = "0") Long providerId,
        @RequestParam(required = false, defaultValue = "") String metalId,
        @RequestParam(required = false, defaultValue = "") String orderStatus,
        @RequestParam(required = false, defaultValue = "") String typeOfTransaction,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(defaultValue = "create_ts") String sortBy,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        OrderFilter orderFilter = OrderFilter.newBuilder()
            .setUserId(userId)
            .setProviderId(providerId)
            .setMetalId(metalId)
            .setOrderStatus(orderStatus)
            .setTypeOfTransaction(TransactionType.valueOf(typeOfTransaction))
            .setSortBy(sortBy)
            .setPage(page)
            .setPageSize(pageSize)
            .build();

        PaginatedOrders paginatedOrders = orderService.fetchOrders(orderFilter);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, paginatedOrders);
    }

}
