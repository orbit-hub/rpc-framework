package cn.edu.hubu.rpc.core.serialize.impl;


import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.utils.RpcException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @Author hxy
 * @Date 2022/4/6
 */

public class JacksonSerializer implements Serializer {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes)  {

        try {
            return  objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RpcException(e);
        }

    }

    @Override
    public <T> byte[] serialize(T object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RpcException(e);
        }
    }
}
