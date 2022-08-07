package cn.edu.hubu.rpc.core.serialize.impl;

import cn.edu.hubu.rpc.core.serialize.Serializer;

import java.io.*;

/**
 * @Author hxy
 * @Date 2022/4/21
 *
 * jdk Serializer
 */

public class JDKSerializer implements Serializer {
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();
            return ((T) result);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] serialize(T object) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.writeObject(object);
            //bugfix 解决readObject时候出现的eof异常
            output.writeObject(null);
            output.flush();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
