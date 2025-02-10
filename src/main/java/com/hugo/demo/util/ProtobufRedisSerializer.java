package com.hugo.demo.util;

import com.google.protobuf.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import java.io.IOException;

/**
 * A Redis serializer for ProtoBuf objects stored as JSON
 */
public class ProtobufRedisSerializer<T extends Message> implements RedisSerializer<T> {

    private final Class<T> clazz;

    public ProtobufRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            // Serialize ProtoBuf object to JSON string
            String json = ProtoJsonUtil.toJson(t);
            return json.getBytes();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize Protobuf object to JSON", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            // Deserialize JSON back to ProtoBuf object
            String json = new String(bytes);
            return ProtoJsonUtil.fromJson(json, clazz);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON to Protobuf object", e);
        }
    }
}
