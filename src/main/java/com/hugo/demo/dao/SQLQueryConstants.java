package com.hugo.demo.dao;

public final class SQLQueryConstants {

    public static final String FIND_BY_EMAIL_ADDRESS = """
        SELECT * FROM User u WHERE u.emailAddress = :emailAddress
        """;

    public static final String COUNT_USERS_BY_EMAIL_ADDRESS = """
        SELECT COUNT(*) FROM User u WHERE u.emailAddress = ?
        """;

    public static final String ADD_USER = """
         INSERT INTO User (name, phoneNumber, emailAddress, deleted, pinHash, passwordHash)
         VALUES (:name, :phoneNumber, :emailAddress, false, :pinHash, :passwordHash);
        """;

    public static final String ADD_ITEM_PRICE = """
         INSERT INTO LiveItemPrice (metalId, performance, askValue, bidValue, value, dateTime)
         VALUES (:metalId, :performance, :askValue, :bidValue, :value, :dateTime);
        """;

    public static final String UPDATE_USER_TOKEN = """
         UPDATE User SET token = :token, tokenExpiry = :tokenExpiry WHERE emailAddress = :emailAddress;
        """;

    public static final String ADD_LIVE_ITEM_PRICE = """
            INSERT INTO LiveItemPrice (metalId, performance, askValue, bidValue, value, dateTime)
            VALUES (:metalId, :performance, :askValue, :bidValue, :value, :dateTime);
        """;

    public static final String UPDATE_LIVE_ITEM_PRICE = """
            UPDATE LiveItemPrice
            SET performance = :performance, askValue = :askValue, bidValue = :bidValue, value = :value, dateTime = :dateTime
            WHERE metalId = :metalId AND DATE(dateTime) = DATE(:dateTime);
        """;

    public static final String FETCH_LIVE_ITEM_PRICE_BY_TIME = """
            SELECT * FROM LiveItemPrice
            WHERE metalId = :metalId AND dateTime = :dateTime;
        """;

    public static final String FETCH_LIVE_ITEM_PRICE_BY_DATE = """
            SELECT * FROM LiveItemPrice
            WHERE metalId = :metalId AND DATE(dateTime) = DATE(:dateTime)
        """;

    public static final String ADD_PROVIDER = """
        INSERT INTO Provider (providerName, providerAPIUrl) VALUES (:providerName, :providerAPIUrl)
        """;

    public static final String FETCH_PROVIDER_DETAILS_BY_ID = """
        SELECT * FROM Provider WHERE providerId = :providerId
        """;

    public static final String FETCH_PROVIDER_DETAILS_BY_NAME = """
        SELECT * FROM Provider WHERE providerName = :providerName
        """;

    public static final String EDIT_PROVIDER_DETAILS = """
        UPDATE Provider SET providerName = :providerName, providerAPIUrl = :providerAPIUrl WHERE providerId = :providerId
        """;

    public static final String DELETE_PROVIDER = """
        DELETE FROM Provider WHERE providerId = :providerId
        """;

    public static final String FETCH_ALL_PROVIDERS = """
        SELECT * FROM Provider
        """;

    public static final String ADD_PRODUCT = """
            INSERT INTO Product (metalId, providerId, productName, productValue, productDescription, stock)
            VALUES (:metalId, :providerId, :productName, :productValue, :productDescription, :stock)
        """;

    public static final String UPDATE_PRODUCT = """
            UPDATE Product 
            SET productName = :productName, 
                productValue = :productValue, 
                productDescription = :productDescription, 
                stock = :stock, 
                update_ts = CURRENT_TIMESTAMP(6)
            WHERE metalId = :metalId AND providerId = :providerId
        """;

    public static final String DELETE_PRODUCT = """
            DELETE FROM Product 
            WHERE metalId = :metalId AND providerId = :providerId
        """;

    public static final String GET_ALL_PRODUCTS = """
            SELECT * FROM Product
        """;

    public static final String GET_PRODUCTS_BY_PROVIDER_ID = """
            SELECT * FROM Product 
            WHERE providerId = :providerId 
        """;

    public static final String GET_PRODUCTS_BY_METAL_CODE = """
            SELECT * FROM Product 
            WHERE metalId = :metalId 
        """;

    public static final String FETCH_TOTAL_ITEMS = """
        SELECT COUNT(*) FROM Product
        """;

    public static final String FETCH_TOTAL_PAGES = """
        SELECT CEIL(COUNT(*) / :pageSize) FROM Product
        """;

    public static final String UPDATE_DATE_ITEM_RECORD = """
        UPDATE DayItemPrice SET
                    openPrice = COALESCE(:openPrice, openPrice), 
                    closePrice = COALESCE(:closePrice, closePrice), 
                    highPrice = COALESCE(:highPrice, highPrice), 
                    lowPrice = COALESCE(:lowPrice, lowPrice), 
                    update_ts = CURRENT_TIMESTAMP(6) 
                    WHERE metalId = :metalId AND date = :date
        """;

    public static final String ADD_DATE_ITEM_RECORD = """
        INSERT INTO DayItemPrice (metalId, date, openPrice, closePrice, highPrice, lowPrice) 
                    VALUES (:metalId, :date, :openPrice, :closePrice, :highPrice, :lowPrice)
        """;

    public static final String FETCH_DAY_ITEM_PRICE = """
            SELECT metalId, date, openPrice, closePrice, highPrice, lowPrice, created_at, updated_at 
            FROM DayItemPrice 
            WHERE metalId = :metalId AND date = :date
        """;

    public static final String FETCH_DAY_ITEMS = """
        SELECT metalId, date, openPrice, closePrice, highPrice, lowPrice, created_at, updated_at FROM DayItemPrice WHERE 1=1
        """;
}
