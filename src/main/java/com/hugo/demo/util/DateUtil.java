package com.hugo.demo.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.google.protobuf.Timestamp;

public class DateUtil {

    public static Timestamp getTokenExpiry(long secondToAdd) {
        Instant now = Instant.now();

        Instant tokenExpiryInstant = now.plus(secondToAdd, ChronoUnit.SECONDS);

        return Timestamp.newBuilder()
            .setSeconds(tokenExpiryInstant.getEpochSecond())
            .setNanos(tokenExpiryInstant.getNano())
            .build();
    }

    public static String convertTimestampToString(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

        java.time.LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    public static Timestamp convertToProtoTimestamp(java.sql.Timestamp sqlTimestamp) {
        if (sqlTimestamp == null) {
            return Timestamp.getDefaultInstance();
        }

        return Timestamp.newBuilder()
            .setSeconds(sqlTimestamp.getTime() / 1000)
            .setNanos(sqlTimestamp.getNanos())
            .build();
    }
}
