package ru.axel.catty.launcher.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigApp implements IConfig {
    private final Logger logger;
    private int port;
    private String protocol;
    private int limitAllocateBufferForRequest;
    private int poolLimit;
    private InetSocketAddress hostAddress;
    private int answerTimeout;
    private long timeToReadBuffer;
    protected final Config config;

    public ConfigApp(String pathFromResource, Logger loggerInstance) {
        logger = loggerInstance;
        config = ConfigFactory.parseResources(pathFromResource);

        init();
    }

    public ConfigApp(Config config, Logger loggerInstance) {
        logger = loggerInstance;
        this.config = config;

        init();
    }

    private void init() {
        port = getConfigOrDefault("catty.port", 8080, config);
        protocol = getConfigOrDefault("catty.protocol", "http", config);
        limitAllocateBufferForRequest = getConfigOrDefault(
            "catty.limitAllocateBufferForRequest",
            5_242_880, // 5mb
            config
        );
        poolLimit = getConfigOrDefault("catty.poolLimit", 1, config);
        hostAddress = new InetSocketAddress(port);
        answerTimeout = getConfigOrDefault("answerTimeout", 30, config);
        timeToReadBuffer = getConfigOrDefault("timeToReadBuffer", 5, config);

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("Загружена настройка port: " + port);
            logger.config("Загружена настройка protocol: " + protocol);
            logger.config("Загружена настройка limitAllocateBufferForRequest: " + limitAllocateBufferForRequest);
            logger.config("Загружена настройка poolLimit: " + poolLimit);
            logger.config("Загружена настройка answerTimeout: " + answerTimeout);
            logger.config("Загружена настройка timeToReadBuffer: " + timeToReadBuffer);
        }
    }

    @Override
    public InetSocketAddress getHostAddress() {
        return hostAddress;
    }

    @Override
    public int getPoolLimit() {
        return poolLimit;
    }

    @Override
    public int getLimitAllocateBufferForRequest() {
        return limitAllocateBufferForRequest;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getAnswerTimeout() {
        return answerTimeout;
    }

    @Override
    public long getTimeToReadBuffer() {
        return timeToReadBuffer;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }
}
