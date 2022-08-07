package cn.edu.hubu.rpc.core.serialize.impl;


import cn.edu.hubu.rpc.core.serialize.Serializer;
import cn.edu.hubu.rpc.core.utils.RpcException;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author hxy
 * @Date 2022/4/6
 */

public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj){
        ByteArrayOutputStream os = null;
        Hessian2Output ho = null;
        try {
            os = new ByteArrayOutputStream();
            ho = new Hessian2Output(os);
            ho.writeObject(obj);
            ho.flush();
            byte[] result = os.toByteArray();
            return result;
        } catch (IOException e) {
            throw new RpcException(e);
        } finally {
            try {
                if (ho !=null) ho.close();
            } catch (IOException e) {
                throw new RpcException(e);
            }
            try {
                if (os !=null) os.close();
            } catch (IOException e) {
                throw new RpcException(e);
            }
        }

    }
    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes){
        ByteArrayInputStream is = null;
        Hessian2Input hi = null;
        try {
            is = new ByteArrayInputStream(bytes);
            hi = new Hessian2Input(is);
            T result = (T) hi.readObject();
            return result;
        } catch (IOException e) {
            throw new RpcException(e);
        } finally {
            try {
                if (hi!=null) {
                    hi.close();
                }
            } catch (Exception e) {
                throw new RpcException(e);
            }
            try {
                if (is!=null) {
                    is.close();
                }
            } catch (IOException e) {
                throw new RpcException(e);
            }
        }
    }
    //static class Student implements Serializable {
//        int no;
//        String name;
//
//    public int getNo() {
//        return no;
//    }
//
//    public void setNo(int no) {
//        this.no = no;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public String toString() {
//        return "Student{" +
//                "no=" + no +
//                ", name='" + name + '\'' +
//                '}';
//    }
//}

//    public static void main(String[] args) throws IOException {
//        Student student = new Student();
//        student.setNo(101);
//        student.setName("HESSIAN");
////把student对象转化为byte数组
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        Hessian2Output output = new Hessian2Output(bos);
//        output.writeObject(student);
//        output.flushBuffer();
//        byte[] data = bos.toByteArray();
//        bos.close();
////把刚才序列化出来的byte数组转化为student对象
//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        Hessian2Input input = new Hessian2Input(bis);
//        Student deStudent = (Student) input.readObject();
//        input.close();
//        System.out.println(deStudent);
//    }
}
