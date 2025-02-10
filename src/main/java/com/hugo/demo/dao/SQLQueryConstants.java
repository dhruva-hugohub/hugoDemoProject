package com.hugo.demo.dao;

public final class SQLQueryConstants {

//User Queries
    public static final String ADD_USER = """
         INSERT INTO User (name, phoneNumber, emailAddress, profileImage, deleted, pinHash, passwordHash)
         VALUES (:name, :phoneNumber, :emailAddress, :profileImage, false, :pinHash, :passwordHash)
        """;

    public static final String UPDATE_USER_BASE_QUERY = """
           UPDATE User SET
        """;

    public static final String FIND_USER_BASE_QUERY = "SELECT * FROM User WHERE 1=1 ";


    public static final String COUNT_USERS_BY_FIELD = " SELECT COUNT(*) FROM User u WHERE ";

    public static final String MARK_USER_DELETED = """
        UPDATE User
         SET deleted = true
         WHERE userId =:userId 
        """;



    // Live Item Price Queries
    public static final String ADD_LIVE_ITEM_PRICE = """
            INSERT INTO LiveItemPrice (metalId,providerId, performance, askValue, bidValue, value, dateTime)
            VALUES (:metalId,:providerId, :performance, :askValue, :bidValue, :value, :dateTime);
        """;

    public static final String UPDATE_LIVE_ITEM_PRICE = """
            UPDATE LiveItemPrice
            SET performance = :performance, askValue = :askValue, bidValue = :bidValue, value = :value, dateTime = :dateTime
            WHERE metalId = :metalId AND DATE(dateTime) = DATE(:dateTime) AND providerId = :providerId;
        """;

    public static final String FETCH_LIVE_ITEM_PRICE_BY_TIME = """
            SELECT * FROM LiveItemPrice
            WHERE metalId = :metalId AND dateTime = :dateTime AND providerId = :providerId;
        """;

    public static final String FETCH_LIVE_ITEM_PRICE_BY_DATE = """
            SELECT * FROM LiveItemPrice
            WHERE metalId = :metalId AND DATE(dateTime) = DATE(:dateTime) AND providerId = :providerId;
        """;

    // Provider Queries
    public static final String ADD_PROVIDER = """
        INSERT INTO Provider (providerName, providerAPIUrl, schedulerTimePeriod) VALUES (:providerName, :providerAPIUrl, :schedulerTimePeriod)
        """;

    public static final String FETCH_PROVIDER_DETAILS_BY_ID = """
        SELECT * FROM Provider WHERE providerId = :providerId
        """;

    public static final String FETCH_PROVIDER_DETAILS_BY_NAME = """
        SELECT * FROM Provider WHERE providerName = :providerName
        """;

    public static final String EDIT_PROVIDER_DETAILS = """
        UPDATE Provider SET providerName = :providerName, providerAPIUrl = :providerAPIUrl WHERE providerId = :providerId WHERE schedulerTimePeriod  = :schedulerTimePeriod
        """;

    public static final String DELETE_PROVIDER = """
        DELETE FROM Provider WHERE providerId = :providerId
        """;

    public static final String FETCH_ALL_PROVIDERS = """
        SELECT * FROM Provider
        """;

    public static final String FETCH_PROVIDERS = """
        SELECT * FROM Provider 
        WHERE (:providerName IS NULL OR providerName LIKE :providerName)
        ORDER BY CASE 
            WHEN :sortBy = 'providerId' THEN providerId
            WHEN :sortBy = 'providerName' THEN providerName
            WHEN :sortBy = 'schedulerTimePeriod' THEN schedulerTimePeriod
            ELSE providerId
        END
        LIMIT :pageSize OFFSET :offset
        """;

    public static final String FETCH_PROVIDER_COUNT = """
        SELECT COUNT(*) FROM Provider 
        WHERE (:providerName IS NULL OR providerName LIKE :providerName)
        """;


    // Product Queries
    public static final String ADD_PRODUCT = """
            INSERT INTO Product (metalId, providerId, productName, productValue, productDescription, stock)
            VALUES (:metalId, :providerId, :productName, :productValue, :productDescription, :stock)
        """;

    public static final String UPDATE_PRODUCT_BASE_QUERY = """
            UPDATE Product SET
        """;
    public static final String UPDATE_STOCK = """
            UPDATE Product 
            SET stock = stock + :stock
            WHERE metalId = :metalId AND providerId = :providerId
        """;

    public static final String DELETE_PRODUCT = """
            DELETE FROM Product 
            WHERE metalId = :metalId AND providerId = :providerId
        """;

    public static final String FETCH_PRODUCT_DETAILS_BY_METAL_AND_ID_PROVIDER_ID = """
            SELECT * FROM Product 
            WHERE metalId = :metalId AND providerId = :providerId
        """;

    // Date Item Queries
    public static final String UPDATE_DATE_ITEM_RECORD_BASE_QUERY = """
        UPDATE DayItemPrice SET
        """;
    public static final String FETCH_DATE_ITEM_RECORD_BASE_QUERY = """
        SELECT * FROM DayItemPrice WHERE 1=1
        """;

    public static final String ADD_DATE_ITEM_RECORD = """
        INSERT INTO DayItemPrice (metalId, date, providerId, openPrice, closePrice, highPrice, lowPrice) 
                    VALUES (:metalId, :date, :providerId, :openPrice, :closePrice, :highPrice, :lowPrice)
        """;

    public static final String FETCH_DAY_ITEM_PRICE = """
            SELECT * FROM DayItemPrice WHERE metalId = :metalId AND date = :date AND providerId = :providerId
        """;

    public static final String FETCH_DAY_ITEMS = """
        SELECT metalId, providerId, date, openPrice, closePrice, highPrice, lowPrice FROM DayItemPrice WHERE 1=1
        """;

    // Order Queries
    public static final String CREATE_ORDER = """
        INSERT INTO OrderTable (metalId, userId, providerId, orderStatus, closingBalance, 
                            amount, quantity, typeOfTransaction, itemsQuantity) 
        VALUES (:metalId, :userId, :providerId, :orderStatus, :closingBalance, 
                :amount, :quantity, :typeOfTransaction, :itemsQuantity);
        """;

    public static final String UPDATE_ORDER_STATUS = """
        UPDATE OrderTable 
        SET orderStatus = :orderStatus, itemsQuantity = :itemsQuantity
        WHERE orderId = :orderId;
        """;

    public static final String GET_ORDER_BY_ID = "SELECT orderId, metalId, userId, providerId, orderStatus, closingBalance, amount, quantity, itemsQuantity, typeOfTransaction, create_ts, update_ts FROM OrderTable WHERE orderId = :orderId";

    public static final String GET_ORDER_BASE_QUERY = "SELECT orderId, metalId, userId, providerId, orderStatus, closingBalance, amount, quantity, itemsQuantity, typeOfTransaction, create_ts, update_ts  FROM OrderTable WHERE 1=1";

    public static final String VERIFY_ORDER_BY_ID = """
        SELECT COUNT(*)
        FROM OrderTable
        WHERE orderId = :orderId;
        """;

    // Wallet Queries
    public static final String FIND_WALLET_BY_ID = """
        SELECT * FROM Wallet u WHERE u.walletId = :walletId
        """;

    public static final String FIND_WALLET_BY_USER_ID = """
        SELECT * FROM Wallet u WHERE u.userId = :userId
        """;

    public static final String COUNT_WALLET_BY_FIELD = " SELECT COUNT(*) FROM Wallet u WHERE ";

    public static final String UPDATE_WALLET = """
             UPDATE Wallet
        SET walletBalance = walletBalance + :walletBalance,
        WHERE userId = :userId
        """;

    public static final String ADD_WALLET = """
             INSERT INTO Wallet (userId, walletBalance) VALUES (:userId, :walletBalance)
        """;


    // User Quantity Queries
    public static final String UPDATE_USER_QUANTITY = """
             UPDATE Quantity
        SET quantity = quantity + :quantity,
        WHERE metalId = :metalId AND userId = :userId
        """;

    public static final String INSERT_USER_QUANTITY = """
             INSERT INTO Quantity (metalId, userId, quantity) VALUES (:metalId, :userId, :quantity)
        """;

    public static final String GET_USER_QUANTITY_BASE_QUERY = "SELECT metalId, userId, quantity, create_ts, update_ts  FROM Quantity WHERE 1=1";

    // Alert Queries
    public static final String FETCH_ALERT_BASE_QUERY = """
            SELECT * FROM Alert
            WHERE 1=1 AND
        """;

    public static final String UPDATE_ALERT_BASE_QUERY = " UPDATE Alert SET ";

    public static final String ADD_ALERT = """
        INSERT INTO Alert (userId, metalId, providerId, FcmToken, email, minPrice, maxPrice, typeOfAlert, expirationDate) VALUES (:userId, :metalId, :providerId, :FcmToken, :email, :minPrice, :maxPrice, :typeOfAlert, :expirationDate)
        """;


    // Currency Queries
    public static final String ADD_CURRENCY = """
            INSERT INTO Currency (currencyCode, currencyName, value)
            VALUES (currencyCode, currencyName, value)
        """;

    public static final String UPDATE_CURRENCY_BASE_QUERY = """
            UPDATE Currency SET 
        """;

    public static final String DELETE_CURRENCY = """
            DELETE FROM Currency
            WHERE currencyCode = :currencyCode
        """;

    public static final String FETCH_DETAILS_BY_CURRENCY_CODE = """
            SELECT * FROM Currency WHERE currencyCode = :currencyCode
        """;

    public static final String FETCH_ALL_CURR = """
        SELECT * FROM Currency """;
}
