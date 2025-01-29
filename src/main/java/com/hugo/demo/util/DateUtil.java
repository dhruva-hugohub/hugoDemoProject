package com.hugo.demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.google.protobuf.Timestamp;

public class DateUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());

    public static String convertTimestampToString(Timestamp timestamp) {
        return formatter.format(Instant.ofEpochSecond(timestamp.getSeconds()));
    }

    public static Timestamp convertToProtoTimestamp(java.sql.Timestamp timestamp) {
        return Timestamp.newBuilder()
            .setSeconds(timestamp.getTime() / 1000)
            .setNanos((int) (timestamp.getTime() % 1000) * 1_000_000)
            .build();
    }

    public static Timestamp convertSqlTimestampToProtoTimestamp(java.sql.Timestamp sqlTimestamp) {
        if (sqlTimestamp == null) {
            return null;
        }

        long seconds = sqlTimestamp.getTime() / 1000;  // Convert milliseconds to seconds
        int nanos = (int) (sqlTimestamp.getNanos());  // Get the nanos part

        // Create and return a new google.protobuf.Timestamp
        return Timestamp.newBuilder()
            .setSeconds(seconds)
            .setNanos(nanos)
            .build();
    }

    // Custom method to convert date string with timezone to Timestamp
    public static java.sql.Timestamp convertStringToTimestamp(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }

        try {
            // Define the format of the date string including the timezone
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            java.util.Date parsedDate = sdf.parse(dateTimeString);
            return new java.sql.Timestamp (parsedDate.getTime());  // Convert to java.sql.Timestamp
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + dateTimeString, e);
        }
    }

    public static String convertTimestampToDateString(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(timestamp);
    }


}
