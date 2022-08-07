package cn.edu.hubu.rpc.core.remoting.codec;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Author hxy
 * @Date 2022/4/21
 */

public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = 5359096060555795690L;

    private short magicNumber = 19812;

    private int contentLength;

    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" +
                "contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
