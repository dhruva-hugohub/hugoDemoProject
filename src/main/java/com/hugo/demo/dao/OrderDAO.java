package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.order.OrderEntity;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;

public interface OrderDAO {

    OrderEntity createOrder(OrderEntity order);

    OrderEntity updateOrderStatus(OrderEntity order);

    Optional<OrderEntity> getOrderDetailsByOrderId(long orderId);

    boolean checkOrderExistsByOrderId(long orderId);

    List<OrderEntity> getOrderDetailsByUserId(long userId);

    List<OrderEntity> fetchPendingOrders();

    PaginatedOrders fetchOrders(OrderFilter filter);

}
