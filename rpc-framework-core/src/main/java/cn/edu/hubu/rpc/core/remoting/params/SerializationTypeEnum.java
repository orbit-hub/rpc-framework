package cn.edu.hubu.rpc.core.remoting.params;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public enum SerializationTypeEnum {

    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    JACKSON((byte) 0x03, "jackson"),
    JDK((byte) 0x04, "jdk"),
    HESSIAN((byte) 0X05, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    SerializationTypeEnum(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SerializationTypeEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
