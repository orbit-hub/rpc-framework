package cn.edu.hubu.rpc.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author hxy
 * @Date 2022/4/12
 */

public class ThrowableUtil {

    /**
     * parse error to string
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String errorMsg = stringWriter.toString();
        return errorMsg;
    }
}