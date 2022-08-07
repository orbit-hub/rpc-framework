package cn.edu.hubu.rpc.core.remoting.message;



import java.io.Serializable;
import java.util.Arrays;

/**
 * @Author hxy
 * @Date 2022/4/7
 */
public class RpcRequestMessage implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;
    /**
     * 调用的接口全限定名，服务端根据它找到实现
     */
    private String interfaceName;
    /**
     * 调用接口中的方法名
     */
    private String methodName;
    /**
     * 方法参数值数组
     */
    private Object[] parameterValue;
    /**
     * 方法参数类型数组
     */
    private Class<?>[] parameterTypes;

    private String version;

    private String group;

    private long createMillisTime;

    private String accessToken;
    /**
     * 方法返回类型
     */
    private Class<?> returnType;

    public RpcRequestMessage() {
    }

    public RpcRequestMessage(String requestId, String interfaceName, String methodName, Object[] parameterValue, Class<?>[] parameterTypes, String version, String group, long createMillisTime, String accessToken, Class<?> returnType) {
        this.requestId = requestId;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterValue = parameterValue;
        this.parameterTypes = parameterTypes;
        this.version = version;
        this.group = group;
        this.createMillisTime = createMillisTime;
        this.accessToken = accessToken;
        this.returnType = returnType;
    }

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(Object[] parameterValue) {
        this.parameterValue = parameterValue;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getCreateMillisTime() {
        return createMillisTime;
    }

    public void setCreateMillisTime(long createMillisTime) {
        this.createMillisTime = createMillisTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "RpcRequestMessage{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterValue=" + Arrays.toString(parameterValue) +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", version='" + version + '\'' +
                ", group='" + group + '\'' +
                ", createMillisTime=" + createMillisTime +
                ", accessToken='" + accessToken + '\'' +
                ", returnType=" + returnType +
                '}';
    }
}
