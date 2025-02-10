package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.dao.OrderDAO;
import com.hugo.demo.order.OrderEntity;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade {

    private final OrderDAO orderDAO;

    public OrderFacade(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    public OrderEntity createOrder(OrderEntity order){
        return orderDAO.createOrder(order);
    }

    public OrderEntity updateOrderStatus(OrderEntity order){
        return orderDAO.updateOrderStatus(order);
    }

    public Optional<OrderEntity> getOrderDetailsByOrderId(long orderId){
        return orderDAO.getOrderDetailsByOrderId(orderId);
    }

    public boolean checkOrderExistsByOrderId(long orderId){
        return orderDAO.checkOrderExistsByOrderId(orderId);
    }

    public List<OrderEntity> getOrderDetailsByUserId(long userId){
        return orderDAO.getOrderDetailsByUserId(userId);
    }

    public List<OrderEntity> fetchPendingOrders(){
        return orderDAO.fetchPendingOrders();
    }

    public PaginatedOrders fetchOrders(OrderFilter filter){
        return orderDAO.fetchOrders(filter);
    }
}
