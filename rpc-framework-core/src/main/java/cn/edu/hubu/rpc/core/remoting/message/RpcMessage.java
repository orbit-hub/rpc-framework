package cn.edu.hubu.rpc.core.remoting.message;

import java.util.Objects;

/**
 * @Author hxy
 * @Date 2022/4/7
 */

public class RpcMessage {
    /**
     * rpc message type
     */
    private byte messageType;
    /**
     * serialization type
     */
    private byte codec;
    /**
     * compress type
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getCodec() {
        return codec;
    }

    public void setCodec(byte codec) {
        this.codec = codec;
    }

    public byte getCompress() {
        return compress;
    }

    public void setCompress(byte compress) {
        this.compress = compress;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public RpcMessage(byte messageType, byte codec, byte compress, int requestId, Object data) {
        this.messageType = messageType;
        this.codec = codec;
        this.compress = compress;
        this.requestId = requestId;
        this.data = data;
    }

    public RpcMessage() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcMessage that = (RpcMessage) o;
        return messageType == that.messageType &&
                codec == that.codec &&
                compress == that.compress &&
                requestId == that.requestId &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, codec, compress, requestId, data);
    }

    @Override
    public String toString() {
        return "RpcMessage{" +
                "messageType=" + messageType +
                ", codec=" + codec +
                ", compress=" + compress +
                ", requestId=" + requestId +
                ", data=" + data +
                '}';
    }
}
