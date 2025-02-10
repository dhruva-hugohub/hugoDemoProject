package com.hugo.demo.dao.impl;

import static com.hugo.demo.util.DateUtil.convertProtoTimestampToSqlTimestamp;
import static com.hugo.demo.util.DateUtil.convertSqlTimestampToProtoTimestamp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hugo.demo.dao.OrderDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.enums.typeOfTransaction.TransactionType;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.order.OrderEntity;
import com.hugo.demo.order.OrderFilter;
import com.hugo.demo.order.PaginatedOrders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDAOImpl implements OrderDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public OrderDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public OrderEntity createOrder(OrderEntity order) {
        String sql = SQLQueryConstants.CREATE_ORDER;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", order.getMetalId())
            .addValue("userId", order.getUserId())
            .addValue("providerId", order.getProviderId())
            .addValue("orderStatus", order.getOrderStatus())
            .addValue("closingBalance", order.getClosingBalance())
            .addValue("amount", order.getAmount())
            .addValue("quantity", order.getQuantity())
            .addValue("typeOfTransaction", order.getTypeOfTransaction().name())
            .addValue("itemsQuantity", order.getItemsQuantity());

        try {
            executeUpdate(sql, params, "Failed to Create Order");
            Optional<OrderEntity> orderEntityResponse = getOrderDetailsByUserId(order.getUserId()).stream().findFirst();
            return orderEntityResponse.orElse(order);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to create order", e);
        }
    }

    @Override
    public OrderEntity updateOrderStatus(OrderEntity order) {
        String sql = SQLQueryConstants.UPDATE_ORDER_STATUS;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("orderId", order.getOrderId())
            .addValue("orderStatus", order.getOrderStatus())
            .addValue("itemsQuantity", order.getItemsQuantity())
            .addValue("closingBalance", order.getClosingBalance());

        try {
            executeUpdate(sql, params, "Failed to Update Order");
            Optional<OrderEntity> orderEntityResponse = getOrderDetailsByOrderId(order.getOrderId());
            return orderEntityResponse.orElse(null);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to update order status", e);
        }
    }

    @Override
    public Optional<OrderEntity> getOrderDetailsByOrderId(long orderId) {
        String sql = SQLQueryConstants.GET_ORDER_BY_ID;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("orderId", orderId);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToOrderEntity(rs));
                }
                return Optional.empty();
            });
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to fetch order details", e);
        }
    }

    @Override
    public boolean checkOrderExistsByOrderId(long orderId) {
        String sql = SQLQueryConstants.VERIFY_ORDER_BY_ID;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("orderId", orderId);

        try {
            Integer result = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to fetch order details", e);
        }
    }

    @Override
    public List<OrderEntity> getOrderDetailsByUserId(long userId) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = buildBaseQuery();
        query.append(" AND userId =:userId");
        query.append(" ORDER BY create_ts DESC");
        params.put("userId", userId);


        try {
            return fetchOrdersFromDatabase(query.toString(), params);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to fetch orders for user", e);
        }
    }

    @Override
    public PaginatedOrders fetchOrders(OrderFilter filter) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = buildBaseQuery();
        StringBuilder countQuery = buildBaseCountQuery();

        query = applyChecksToFilter(filter, params, query);
        countQuery = applyChecksToFilter(filter, params, countQuery);

        Integer totalItems = getTotalItems(countQuery.toString(), params);

        if (filter.getPageSize() > 0 && filter.getPage() > 0) {
            query.append(" LIMIT :pageSize OFFSET :offset");
            params.put("pageSize", filter.getPageSize());
            params.put("offset", (filter.getPage() - 1) * filter.getPageSize());
        }

        int totalPages = calculateTotalPages(totalItems, filter.getPageSize());
        boolean hasPreviousPage = filter.getPage() > 1;
        boolean hasNextPage = filter.getPage() < totalPages;

        List<OrderEntity> orders = fetchOrdersFromDatabase(query.toString(), params);

        return PaginatedOrders.newBuilder()
            .setTotalPages(totalPages)
            .setTotalItems(totalItems)
            .setHasPreviousPage(hasPreviousPage)
            .setHasNextPage(hasNextPage)
            .addAllOrders(orders)
            .build();
    }

    private StringBuilder applyChecksToFilter(OrderFilter filter, Map<String, Object> params, StringBuilder query) {
        applyUserIdFilter(filter.getUserId(), query, params);
        applyProviderIdFilter(filter.getProviderId(), query, params);
        applyMetalIdFilter(filter.getMetalId(), query, params);
        applyOrderStatusFilter(filter.getOrderStatus(), query, params);
        applyTransactionTypeFilter(filter.getTypeOfTransaction().name(), query, params);
        applyDateRangeFilter(convertProtoTimestampToSqlTimestamp(filter.getStartDate()), convertProtoTimestampToSqlTimestamp(filter.getEndDate()),
            query, params);
        applySorting(filter.getSortBy(), query);
        return query;
    }

    private void applyUserIdFilter(Long userId, StringBuilder query, Map<String, Object> params) {
        if (userId > 0) {
            query.append(" AND userId = :userId");
            params.put("userId", userId);
        }
    }

    private void applyProviderIdFilter(Long providerId, StringBuilder query, Map<String, Object> params) {
        if (providerId > 0) {
            query.append(" AND providerId = :providerId");
            params.put("providerId", providerId);
        }
    }

    private void applyMetalIdFilter(String metalId, StringBuilder query, Map<String, Object> params) {
        if (metalId != null && !metalId.isEmpty()) {
            query.append(" AND metalId = :metalId");
            params.put("metalId", metalId);
        }
    }

    private void applyOrderStatusFilter(String orderStatus, StringBuilder query, Map<String, Object> params) {
        if (orderStatus != null && !orderStatus.isEmpty()) {
            query.append(" AND orderStatus = :orderStatus");
            params.put("orderStatus", orderStatus);
        }
    }

    private void applyTransactionTypeFilter(String typeOfTransaction, StringBuilder query, Map<String, Object> params) {
        if (typeOfTransaction != null && !typeOfTransaction.isEmpty()) {
            query.append(" AND typeOfTransaction = :typeOfTransaction");
            params.put("typeOfTransaction", typeOfTransaction);
        }
    }

    private void applyDateRangeFilter(Timestamp startDate, Timestamp endDate, StringBuilder query, Map<String, Object> params) {
        if (startDate != null) {
            query.append(" AND create_ts >= :startDate");
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            query.append(" AND create_ts <= :endDate");
            params.put("endDate", endDate);
        }
    }

    private void applySorting(String sortBy, StringBuilder query) {
        if (sortBy != null && !sortBy.isEmpty()) {
            query.append(" ORDER BY ").append(sortBy);
        }
    }

    @Override
    public List<OrderEntity> fetchPendingOrders() {
        Map<String, Object> params = new HashMap<>();
        StringBuilder query = buildBaseQuery();
        query.append(" AND orderStatus = :orderStatus");
        params.put("orderStatus", "pending");

        return fetchOrdersFromDatabase(query.toString(), params);
    }

    private StringBuilder buildBaseCountQuery() {
        return new StringBuilder("SELECT COUNT(*) FROM Orders WHERE 1=1");
    }


    private StringBuilder buildBaseQuery() {
        return new StringBuilder(SQLQueryConstants.GET_ORDER_BASE_QUERY);
    }

    private Integer getTotalItems(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
    }

    private int calculateTotalPages(int totalItems, Integer pageSize) {
        return (pageSize == null || pageSize == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
    }

    private List<OrderEntity> fetchOrdersFromDatabase(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToOrderEntity(rs));
    }

    private void executeUpdate(String sql, MapSqlParameterSource params, String errorMessage) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                throw new InternalServerErrorException(errorMessage);
            }
        } catch (Exception e) {
            throw new InternalServerErrorException(errorMessage, e);
        }
    }


    private OrderEntity mapRowToOrderEntity(ResultSet rs) throws SQLException {
        return OrderEntity.newBuilder().setOrderId(
                rs.getInt("orderId")).setMetalId(
                rs.getString("metalId")).setUserId(
                rs.getInt("userId")).setProviderId(
                rs.getInt("providerId")).setOrderStatus(
                rs.getString("orderStatus")).setClosingBalance(
                rs.getDouble("closingBalance")).setAmount(
                rs.getDouble("amount")).setQuantity(
                rs.getDouble("quantity")).setTypeOfTransaction(TransactionType.valueOf(
                rs.getString("typeOfTransaction"))).setItemsQuantity(
                rs.getString("itemsQuantity")).setCreatedAt(
                convertSqlTimestampToProtoTimestamp(rs.getTimestamp("create_ts")))
            .setUpdatedAt(convertSqlTimestampToProtoTimestamp(rs.getTimestamp("update_ts"))).build();
    }
}
