package com.hugo.demo.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

@SuppressWarnings("all")
public class ProtobufUtils {

    public static Object valueToObject(Value value) {
        return switch (value.getKindCase()) {
            case NULL_VALUE -> null;
            case NUMBER_VALUE -> value.getNumberValue();
            case STRING_VALUE -> value.getStringValue();
            case BOOL_VALUE -> value.getBoolValue();
            case STRUCT_VALUE -> structToMap(value.getStructValue());
            case LIST_VALUE -> listValueToList(value.getListValue());
            default -> throw new IllegalArgumentException("Unknown Value type: " + value.getKindCase());
        };
    }

    public static Map<String, Object> structToMap(Struct struct) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Value> entry : struct.getFieldsMap().entrySet()) {
            map.put(entry.getKey(), valueToObject(entry.getValue()));
        }
        return map;
    }

    public static List<Object> listValueToList(ListValue listValue) {
        return listValue.getValuesList().stream()
            .map(ProtobufUtils::valueToObject)
            .toList();
    }

    private ProtobufUtils() {

    }
}
