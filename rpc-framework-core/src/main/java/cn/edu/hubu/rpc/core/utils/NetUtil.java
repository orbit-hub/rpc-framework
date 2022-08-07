package cn.edu.hubu.rpc.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @Author hxy
 * @Date 2022/4/12
 */

public class NetUtil {
    private static Logger logger = LoggerFactory.getLogger(NetUtil.class);

    /**
     * check port used
     *
     * @param port
     * @return
     */
    public static boolean isPortUsed(int port) {
        boolean used = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            used = false;
        } catch (IOException e) {
            logger.info(">>>>>>>>>>> rpc, port[{}] is in use.", port);
            used = true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.info("");
                }
            }
        }
        return used;
    }

}
