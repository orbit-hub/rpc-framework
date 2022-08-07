package cn.edu.hubu.rpc.core.remoting.message;


import cn.edu.hubu.rpc.core.remoting.params.RpcResponseCodeEnum;

import java.io.Serializable;

/**
 * @Author hxy
 * @Date 2022/4/7
 */


public class RpcResponseMessage<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;
    public void setErrorMsg(String errorMsg) {
        this.message = errorMsg;
    }
    public String getErrorMsg() {
        return message;
    }
    public static <T> RpcResponseMessage<T> success(T data, String requestId) {
        RpcResponseMessage<T> response = new RpcResponseMessage<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponseMessage<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponseMessage<T> response = new RpcResponseMessage<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

    public RpcResponseMessage() {
    }

    public RpcResponseMessage(String requestId, Integer code, String message, T data) {
        this.requestId = requestId;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponseMessage{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
