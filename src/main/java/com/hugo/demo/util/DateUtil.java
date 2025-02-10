package com.hugo.demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.google.protobuf.Timestamp;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;

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

    public static java.sql.Timestamp convertProtoTimestampToSqlTimestamp(Timestamp protoTimestamp) {
        if (protoTimestamp == null) {
            return null;
        }
        Instant instant = Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
        return java.sql.Timestamp.from(instant);
    }

    public static Timestamp convertSqlTimestampToProtoTimestamp(java.sql.Timestamp sqlTimestamp) {
        try {
            if (sqlTimestamp == null) {
                return null;
            }

            long seconds = sqlTimestamp.getTime() / 1000;
            int nanos = (sqlTimestamp.getNanos());

            return Timestamp.newBuilder()
                .setSeconds(seconds)
                .setNanos(nanos)
                .build();
        } catch (Exception e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
    }

    public static java.sql.Timestamp convertStringToTimestamp(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            java.util.Date parsedDate = sdf.parse(dateTimeString);
            return new java.sql.Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Invalid date format: " + dateTimeString);
        }
    }
}
