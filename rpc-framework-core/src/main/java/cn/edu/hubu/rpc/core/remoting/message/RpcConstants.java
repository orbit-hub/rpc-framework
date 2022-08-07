package cn.edu.hubu.rpc.core.remoting.message;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 *  Constant
 * @Author hxy
 * @Date 2022/4/7
 */

public class RpcConstants {

    public static final short MAGIC_NUMBER=19812;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //version information
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    public static final String DEFAULT_DECODE_CHAR = "$_i0#Xsop1_$";

}