package cn.edu.hubu.rpc.core.remoting.server;

import cn.edu.hubu.rpc.core.remoting.params.BaseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hxy
 * @Date 2022/4/7
 *
 * server
 */

public abstract class Server {
    protected static final Logger logger = LoggerFactory.getLogger(Server.class);

    private BaseCallback startedCallback;
    private BaseCallback stopedCallback;

    public void setStartedCallback(BaseCallback startedCallback) {
        this.startedCallback = startedCallback;
    }

    public void setStopedCallback(BaseCallback stopedCallback) {
        this.stopedCallback = stopedCallback;
    }

    /**
     * start server
     *
     * @param rpcProviderFactory
     * @throws Exception
     */
    public abstract void start(final RpcProviderFactory rpcProviderFactory) throws Exception;


    /**
     * stop server
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;

    /**
     * callback when started 注册中心启动时的回调
     */
    public void onStarted() {
        if (startedCallback != null) {
            try {
                startedCallback.run();
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> rpc, server startedCallback error.", e);
            }
        }
    }

    /**
     * callback when stoped 注册中心启关闭时的回调
     */
    public void onStoped() {
        if (stopedCallback != null) {
            try {
                stopedCallback.run();
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> rpc, server stopedCallback error.", e);
            }
        }
    }
}
