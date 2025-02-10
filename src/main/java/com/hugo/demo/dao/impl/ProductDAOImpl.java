package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hugo.demo.dao.ProductDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.product.ProductFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDAOImpl implements ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ProductDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public ProductEntity addProduct(ProductEntity product) {
        String sql = SQLQueryConstants.ADD_PRODUCT;

        if (isProductExists(product.getProviderId(), product.getMetalId())) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR,
                "Provider with metal Id '" + product.getMetalId() + " and provider Id" + product.getProviderId() + "' already exists.");
        }

        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("metalId", product.getMetalId()).addValue("providerId", product.getProviderId())
                .addValue("productName", product.getProductName()).addValue("productValue", product.getProductValue())
                .addValue("productDescription", product.getProductDescription()).addValue("stock", product.getStock());

        try {
            executeUpdate(sql, params, "Failed to add product");
            return fetchProductDetails(Long.parseLong(String.valueOf(product.getProviderId())), product.getMetalId()).orElse(product);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to add product", e);
        }
    }

    @Override
    public ProductEntity updateProduct(ProductEntity product) {

        StringBuilder baseQuery = new StringBuilder(SQLQueryConstants.UPDATE_PRODUCT_BASE_QUERY);
        boolean hasPrevious = false;
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!product.getProductName().isEmpty()) {
            hasPrevious = true;
            baseQuery.append(" productName = :productName");
            params.addValue("productName", product.getMetalId());
        }
        if (product.getProductValue() != 0) {
            if (hasPrevious) {
                baseQuery.append(", ");
            }
            hasPrevious = true;
            baseQuery.append(" productValue = :productValue");
            params.addValue("productValue", product.getProductValue());
        }
        if (product.getStock() != 0) {
            if (hasPrevious) {
                baseQuery.append(", ");
            }
            hasPrevious = true;
            baseQuery.append(" stock = :stock");
            params.addValue("stock", product.getStock());
        }
        if (!product.getProductDescription().isEmpty()) {
            if (hasPrevious) {
                baseQuery.append(", ");
            }
            baseQuery.append(" productDescription = :productDescription");
            params.addValue("productDescription", product.getProductDescription());
        }

        baseQuery.append(" WHERE metalId =:metalId AND providerId = :providerId");
        params.addValue("metalId", product.getMetalId());
        params.addValue("providerId", product.getProviderId());
        if (!isProductExists(product.getProviderId(), product.getMetalId())) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,
                "Provider with metal Id '" + product.getMetalId() + " and provider Id" + product.getProviderId() + "' already exists.");
        }

        try {
            executeUpdate(baseQuery.toString(), params, "Failed to update product");
            return fetchProductDetails(Long.parseLong(String.valueOf(product.getProviderId())), product.getMetalId()).orElse(product);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Unexpected error while updating provider details", e);
        }
    }

    public void updateStock(long providerId, String metalId, int stock) {
        String sql = SQLQueryConstants.UPDATE_STOCK;

        if (!isProductExists(providerId, metalId)) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,
                "Provider with metal Id '" + providerId + " and provider Id" + metalId + "' already exists.");
        }


        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("metalId", metalId).addValue("providerId", providerId)
                .addValue("stock", stock);

        try {
            executeUpdate(sql, params, "Failed to update product");
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Unexpected error while updating provider details", e);
        }
    }

    @Override
    public boolean deleteProduct(String metalId, long providerId) {
        String sql = SQLQueryConstants.DELETE_PRODUCT;

        if (!isProductExists(providerId, metalId)) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"No provider found with id: " + providerId);
        }
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("metalId", metalId).addValue("providerId", providerId);

        try {
           return executeDelete(sql, params);
        } catch (Exception e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"No provider found with id: " + providerId);
        }
    }


    @Override
    public boolean isProductExists(Long providerId, String metalId) {
        return fetchProductDetails(providerId, metalId).isPresent();
    }

    @Override
    public Optional<ProductEntity> fetchProductDetails(Long providerId, String metalId) {
        String sql = SQLQueryConstants.FETCH_PRODUCT_DETAILS_BY_METAL_AND_ID_PROVIDER_ID;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("providerId", providerId);
        params.addValue("metalId", metalId);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToProductEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching product details - ID: {}, Metal Id: {}", providerId, metalId , e);
            return Optional.empty();
        }
    }

    @Override
    public PaginatedProducts fetchProducts(ProductFilter filter) {

        Map<String, Object> params = new HashMap<>();
        StringBuilder query = buildBaseQuery();
        StringBuilder countQuery = buildBaseCountQuery();

        query = applyChecksToFilter(filter, params, query);

        countQuery = applyChecksToFilter(filter, params, countQuery);

        Integer totalItems = getTotalItems(countQuery.toString(), params);

        if(filter.getPageSize() != 0 && filter.getPage() != 0) {
            query.append(" LIMIT :pageSize OFFSET :offset");
            params.put("pageSize", filter.getPageSize());
            params.put("offset", (filter.getPage() - 1) * filter.getPageSize());
        }

        int totalPages = calculateTotalPages(totalItems, filter.getPageSize());
        boolean hasPreviousPage = filter.getPage() > 1;
        boolean hasNextPage = filter.getPage() < totalPages;

        List<ProductEntity> products = fetchProductsFromDatabase(query.toString(), params);

        return PaginatedProducts.newBuilder()
            .setTotalPages(totalPages)
            .setTotalItems(totalItems)
            .setHasPreviousPage(hasPreviousPage)
            .setHasNextPage(hasNextPage)
            .addAllProducts(products)
            .build();
    }

    private StringBuilder applyChecksToFilter(ProductFilter filter, Map<String, Object> params, StringBuilder countQuery) {
        applySearchFilter(filter.getProductName(), countQuery, params);
        applyMetalFilter(filter.getMetalId(), countQuery, params);
        applyProviderIdFilter(filter.getProviderId(), countQuery, params);
        applyStockFilter(filter.getStockLowerLimit(), filter.getStockUpperLimit(), countQuery, params);
        applyProductValueFilter(filter.getProductValueLowerLimit(), filter.getProductValueUpperLimit(), countQuery, params);
        applySorting(filter.getSortBy(), countQuery);
        return countQuery;
    }

    @Override
    public List<ProductEntity> fetchAllProducts() {
        String query = buildBaseQuery().toString();
        return namedParameterJdbcTemplate.query(query, new HashMap<>(), (rs, rowNum) -> mapRowToProductEntity(rs));
    }

    private StringBuilder buildBaseQuery() {
        return new StringBuilder("SELECT * FROM Product WHERE 1=1");
    }

    private StringBuilder buildBaseCountQuery() {
        return new StringBuilder("SELECT COUNT(*) FROM Product WHERE 1=1");
    }

    private void applySearchFilter(String productName, StringBuilder query, Map<String, Object> params) {
        if (productName != null && !productName.isEmpty()) {
            query.append(" AND productName LIKE :productName");
            params.put("productName", "%" + productName + "%");
        }
    }

    private void applyMetalFilter(String metalId, StringBuilder query, Map<String, Object> params) {
        if (metalId != null && !metalId.isEmpty()) {
            query.append(" AND metalId = :metalId");
            params.put("metalId", metalId);
        }
    }

    private void applyProviderIdFilter(long providerId, StringBuilder query, Map<String, Object> params) {
        if (providerId > 0) {
            query.append(" AND providerId = :providerId");
            params.put("providerId", providerId);
        }
    }

    private void applyStockFilter(Integer stockLowerLimit, Integer stockUpperLimit, StringBuilder query, Map<String, Object> params) {
        if(stockLowerLimit != null && stockUpperLimit != null) {
            if (stockLowerLimit > 0) {
                query.append(" AND stock >= :stockLowerLimit");
                params.put("stockLowerLimit", stockLowerLimit);
            }
            if (stockUpperLimit > 0) {
                query.append(" AND stock <= :stockUpperLimit");
                params.put("stockUpperLimit", stockUpperLimit);
            }
        }
    }

    private void applyProductValueFilter(Double productValueLowerLimit, Double productValueUpperLimit, StringBuilder query, Map<String, Object> params) {
        if(productValueLowerLimit != null && productValueUpperLimit != null) {
            if (productValueLowerLimit > 0.00) {
                query.append(" AND productValue >= :productValueLowerLimit");
                params.put("productValueLowerLimit", productValueLowerLimit);
            }
            if (productValueUpperLimit > 0.00) {
                query.append(" AND productValue <= :productValueUpperLimit");
                params.put("productValueUpperLimit", productValueUpperLimit);
            }
        }
    }

    private void applySorting(String sortBy, StringBuilder query) {
        if (sortBy != null && !sortBy.isEmpty()) {
            query.append(" ORDER BY ").append(sortBy);
        }
    }

    private Integer getTotalItems(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForObject(query, params, Integer.class);
    }

    private int calculateTotalPages(int totalItems, Integer pageSize) {
        return (pageSize == null || pageSize == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
    }

    private List<ProductEntity> fetchProductsFromDatabase(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToProductEntity(rs));
    }

    private boolean executeDelete(String sql, MapSqlParameterSource params) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to delete provider", e);
        }
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

    private ProductEntity mapRowToProductEntity(ResultSet rs) throws SQLException {
        return ProductEntity.newBuilder().setMetalId(rs.getString("metalId")).setProviderId(rs.getInt("providerId"))
            .setProductName(rs.getString("productName")).setProductValue(rs.getDouble("productValue"))
            .setProductDescription(rs.getString("productDescription")).setStock(rs.getInt("stock")).build();
    }
}
