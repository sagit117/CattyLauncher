package ru.axel.catty.launcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.ICattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.request.Request;
import ru.axel.catty.engine.request.RequestBuildException;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.response.Response;
import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.launcher.config.IConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class HttpBuilder implements IHttpBuilder {
    private final Logger logger;
    private IConfig config;
    private IRouting routing;
    private Plugins plugins;

    HttpBuilder(Logger loggerImpl) {
        logger = loggerImpl;
    }

    @Contract(value = "_, _ -> this", pure = true)
    public IHttpBuilder setConfig(
        @NotNull Class<? extends IConfig> configClass,
        String pathFromResource
    ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructor = configClass.getDeclaredConstructor(String.class, Logger.class);
        config = constructor.newInstance(pathFromResource, logger);

        return this;
    }
    @Contract(pure = true)
    @Override
    public @NotNull IHttpBuilder setConfig(IConfig configInstance) {
        config = configInstance;
        return this;
    }

    @Override
    public IHttpBuilder setPlugins(Plugins pluginsModule) {
        plugins = pluginsModule;
        return this;
    }

    @Override
    public IHttpBuilder setRouting(IRouting routingModule) {
        routing = routingModule;
        return this;
    }

    public void launch() {
        try(final ICattyEngine engine = new CattyEngine(
            new InetSocketAddress(config.getPort()),
            config.getPoolLimit(),
            config.getLimitAllocateBufferForRequest(),
            Handler::new
        )) {
            engine.startServer();
        } catch (Throwable throwable) {
            throw new RuntimeException("Ошибка запуска сервера!", throwable);
        }
    }

    @Override
    public IConfig getConfig() {
        return config;
    }

    class Handler extends HttpCattyQueryHandler {
        public Handler(AsynchronousSocketChannel clientChannel, int limitBuffer, Logger loggerInstance) {
            super(clientChannel, limitBuffer, loggerInstance);
        }

        @Override
        protected ByteBuffer responseBuffer(ByteBuffer requestBuffer) {
            try {
                final IHttpCattyRequest request = new Request(requestBuffer, logger);
                final IHttpCattyResponse response = new Response(logger);

                try {
                    var route = routing.takeRoute(request);

                    if (route.isPresent()) {
                        request.setRoute(route.get());
                        plugins.exec(request, response);

                        CompletableFuture
                            .runAsync(() -> {
                                try {
                                    request.handle(response);
                                } catch (IOException | URISyntaxException e) {
                                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                                    e.printStackTrace();
                                }
                            }, Executors.newWorkStealingPool(config.getPoolLimit()))
                            .orTimeout(config.getAnswerTimeout(), TimeUnit.SECONDS)
                            .get();
                    } else {
                        response.setResponseCode(ResponseCode.NOT_FOUND);
                    }
                } catch (ExecutionException executionException) { // ожидание ответа превышено
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    executionException.printStackTrace();
                } catch (Throwable exc) {
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    exc.printStackTrace();
                }

                return response.getByteBuffer();
            } catch (RequestBuildException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
