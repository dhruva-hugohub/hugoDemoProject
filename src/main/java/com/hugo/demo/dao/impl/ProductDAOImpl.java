package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.hugo.demo.dao.ProductDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.product.ProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDAOImpl implements ProductDAO {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ProductDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public ProductEntity addProduct(ProductEntity product) {
        String sql = SQLQueryConstants.ADD_PRODUCT;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", product.getMetalId())
            .addValue("providerId", product.getProviderId())
            .addValue("productName", product.getProductName())
            .addValue("productValue", product.getProductValue())
            .addValue("productDescription", product.getProductDescription())
            .addValue("stock", product.getStock());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return product;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to add product", e);
        }
    }

    @Override
    public ProductEntity updateProduct(ProductEntity product) {
        String sql = SQLQueryConstants.UPDATE_PRODUCT;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", product.getMetalId())
            .addValue("providerId", product.getProviderId())
            .addValue("productName", product.getProductName())
            .addValue("productValue", product.getProductValue())
            .addValue("productDescription", product.getProductDescription())
            .addValue("stock", product.getStock());

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected > 0) {
                return product;
            } else {
                throw new RuntimeException("Product not found");
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public void deleteProduct(String metalId, int providerId) {
        String sql = SQLQueryConstants.DELETE_PRODUCT;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", metalId)
            .addValue("providerId", providerId);

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                throw new RuntimeException("Product not found for deletion");
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }


    @Override
    public List<ProductEntity> getAllProducts(String sortField, String sortOrder, Integer page, Integer size) {
        return fetchProducts(SQLQueryConstants.GET_ALL_PRODUCTS, null, sortField, sortOrder, page, size);
    }

    @Override
    public List<ProductEntity> getProductsByProviderId(int providerId, String sortField, String sortOrder, Integer page, Integer size) {
        MapSqlParameterSource additionalParams = new MapSqlParameterSource().addValue("providerId", providerId);
        return fetchProducts(SQLQueryConstants.GET_PRODUCTS_BY_PROVIDER_ID, additionalParams, sortField, sortOrder, page, size);
    }

    @Override
    public List<ProductEntity> getProductsByMetalCode(String metalId, String sortField, String sortOrder, Integer page, Integer size) {
        MapSqlParameterSource additionalParams = new MapSqlParameterSource().addValue("metalId", metalId);
        return fetchProducts(SQLQueryConstants.GET_PRODUCTS_BY_METAL_CODE, additionalParams, sortField, sortOrder, page, size);
    }

    /**
     * Utility method to fetch products based on the provided query and parameters.
     */
    private List<ProductEntity> fetchProducts(
        String baseQuery,
        MapSqlParameterSource additionalParams,
        String sortField,
        String sortOrder,
        Integer page,
        Integer size
    ) {
        sortField = (sortField == null || sortField.isEmpty()) ? "providerId" : sortField;
        sortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "ASC" : sortOrder.toUpperCase();

        String limitClause = "";
        if (page != null && size != null) {
            limitClause = "LIMIT :limit OFFSET :offset";
        }

        String query = String.format("%s ORDER BY %s %s %s", baseQuery, sortField, sortOrder, limitClause);

        MapSqlParameterSource params = (additionalParams != null) ? additionalParams : new MapSqlParameterSource();
        if (!limitClause.isEmpty()) {
            int offset = (page - 1) * size;
            params.addValue("limit", size).addValue("offset", offset);
        }

        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToProductProto(rs));
    }


    @Override
    public int getTotalItems() {
        String sql = SQLQueryConstants.FETCH_TOTAL_ITEMS;

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching total product items", e);
        }
    }

    @Override
    public int getTotalPages(int pageSize) {
        String sql = SQLQueryConstants.FETCH_TOTAL_PAGES;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("pageSize", pageSize);

            return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching total product pages", e);
        }
    }

    private ProductEntity mapRowToProductProto(ResultSet rs) throws SQLException {
        return ProductEntity.newBuilder()
            .setMetalId(rs.getString("metalId"))
            .setProviderId(rs.getInt("providerId"))
            .setProductName(rs.getString("productName"))
            .setProductValue(rs.getDouble("productValue"))
            .setProductDescription(rs.getString("productDescription"))
            .setStock(rs.getInt("stock"))
            .build();
    }
}
