package com.hugo.demo.dao;

public final class SQLQueryConstants {

    public static final String FIND_BY_EMAIL_ADDRESS = """
        SELECT * FROM User u WHERE u.emailAddress = :emailAddress
        """;

    public static final String COUNT_USER_BY_EMAIL_ADDRESS = """
        SELECT COUNT(*) FROM User u WHERE u.emailAddress = :emailAddress
        """;

    public static final String ADD_USER = """
        INSERT INTO User (name, phoneNumber, emailAddress, pinHash, passwordHash, token, tokenExpiry)
        VALUES (:name, :phoneNumber, :emailAddress, :pinHash, :passwordHash, :token, :tokenExpiry);
       """;

    public static final String UPDATE_USER_TOKEN = """
        UPDATE User SET token = :token, tokenExpiry = :tokenExpiry WHERE emailAddress = :emailAddress;
       """;
}
