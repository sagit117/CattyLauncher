package ru.axel.catty.launcher.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ConfigApp implements IConfig {
    private final Logger logger;
    private final int port;
    private final String protocol;
    private final int limitAllocateBufferForRequest;
    private final int poolLimit;
    private final InetSocketAddress hostAddress;
    private final int answerTimeout;
    protected final Config config;

    public ConfigApp(String pathFromResource, Logger loggerInstance) {
        logger = loggerInstance;
        config = ConfigFactory.parseResources(pathFromResource);
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

        logger.config("Загружена настройка port: " + port);
        logger.config("Загружена настройка protocol: " + protocol);
        logger.config("Загружена настройка limitAllocateBufferForRequest: " + limitAllocateBufferForRequest);
        logger.config("Загружена настройка poolLimit: " + poolLimit);
        logger.config("Загружена настройка answerTimeout: " + answerTimeout);
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
    public String getProtocol() {
        return protocol;
    }
}
