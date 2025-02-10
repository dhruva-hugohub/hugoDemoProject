package com.hugo.demo.service;

import com.hugo.demo.api.order.CreateOrderRequestDTO;
import com.hugo.demo.api.order.EditOrderRequestDTO;
import com.hugo.demo.api.order.OrderResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;

public interface OrderService {

    PlainResponseDTO createOrder(CreateOrderRequestDTO orderRequestDTO);

    OrderResponseDTO updateOrderStatus(EditOrderRequestDTO orderRequestDTO);

    OrderResponseDTO getOrderDetailsByOrderId(long orderId, String currencyCode);

    void processPendingOrders();

    PaginatedOrders fetchOrders(OrderFilter orderFilter);

}
