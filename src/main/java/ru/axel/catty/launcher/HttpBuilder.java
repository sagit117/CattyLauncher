package ru.axel.catty.launcher;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.axel.catty.engine.CattyEngine;
import ru.axel.catty.engine.ICattyEngine;
import ru.axel.catty.engine.handler.HttpCattyQueryHandler;
import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.request.ClientInfo;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.request.Request;
import ru.axel.catty.engine.request.RequestBuildException;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.response.Response;
import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.launcher.config.IConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Построитель конвейера обработки запроса/ответа
 */
public final class HttpBuilder implements IHttpBuilder {
    private final Logger logger;
    private IConfig config;
    private IRouting routing;
    private Plugins plugins;
    private RouteExecute afterResponse;
    private RouteExecute withExceptionally;
    private ExecutorService responseExecutors;

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
        responseExecutors = Executors.newWorkStealingPool(config.getPoolLimit());

        logger.config("В построитель добавлен класс конфигурации");
        logger.config("Инициирован пул потоков для предоставления ответа");

        return this;
    }
    @Contract(pure = true)
    @Override
    public @NotNull IHttpBuilder setConfig(IConfig configInstance) {
        config = configInstance;
        responseExecutors = Executors.newWorkStealingPool(config.getPoolLimit());

        logger.config("В построитель добавлен класс конфигурации");
        logger.config("Инициирован пул потоков для предоставления ответа");
        return this;
    }

    @Override
    public IHttpBuilder usePlugins(Plugins pluginsModule) {
        plugins = pluginsModule;
        return this;
    }

    @Override
    public IHttpBuilder useRouting(IRouting routingModule) {
        routing = routingModule;
        return this;
    }

    /**
     * Метод применится после предоставления ответа
     * @param execute обработчик
     * @return построитель
     */
    @Contract(pure = true)
    @Override
    public @Nullable IHttpBuilder useAfterResponse(RouteExecute execute) {
        afterResponse = execute;
        return this;
    }

    /**
     * В методе можно обработать ошибки запроса. Метод запускается перед отправкой ответа.
     * @param execute обработчик
     * @return построитель
     */
    @Override
    public IHttpBuilder useWithExceptionally(RouteExecute execute) {
        withExceptionally = execute;
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

    /**
     * Класс обработчик запроса и ответа
     */
    class Handler extends HttpCattyQueryHandler {
        public Handler(AsynchronousSocketChannel clientChannel, int limitBuffer, Logger loggerInstance) {
            super(clientChannel, limitBuffer, loggerInstance);
        }

        @Override
        protected ByteBuffer responseBuffer(ByteBuffer requestBuffer) {
            try {
                final IHttpCattyRequest request = new Request(requestBuffer, logger);
                final IHttpCattyResponse response = new Response(logger);

                request.setClientInfo(new ClientInfo(client.getLocalAddress(), client.getRemoteAddress()));

                try {
                    final Optional<ICattyRoute> route = routing.takeRoute(request);

                    if (route.isPresent()) {
                        request.setRoute(route.get());
                        plugins.exec(request, response);

                        CompletableFuture
                            .runAsync(() -> {
                                try {
                                    request.handle(response);
                                } catch (IOException | URISyntaxException e) {
                                    // ошибка занесена в запрос и залогирована в методе handle
                                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                                    e.printStackTrace();
                                }
                            }, responseExecutors)
                            .exceptionally((ex) -> {
                                logger.severe("Ошибка в асинхронной обработке запроса: " + ex.getLocalizedMessage());
                                return null;
                            })
                            .orTimeout(config.getAnswerTimeout(), TimeUnit.SECONDS)
                            .get();
                    } else {
                        response.setResponseCode(ResponseCode.NOT_FOUND);
                    }
                } catch (ExecutionException executionException) { // ожидание ответа превышено
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    request.addException(executionException);

                    logger.throwing(HttpBuilder.class.getName(), "responseBuffer", executionException);
                    executionException.printStackTrace();
                } catch (Throwable exc) {
                    response.setResponseCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    request.addException(new Exception(exc));

                    logger.throwing(HttpBuilder.class.getName(), "responseBuffer", exc);
                    exc.printStackTrace();
                }

                if (withExceptionally != null) withExceptionally.exec(request, response);

                final ByteBuffer responseBuffer = response.getByteBuffer();

                CompletableFuture.runAsync(() ->{
                    if (afterResponse != null) {
                        try {
                            afterResponse.exec(request, response);
                        } catch (IOException | URISyntaxException e) {
                            logger.severe("Ошибка пост обработки ответа: " + e.getLocalizedMessage());
                        }
                    }
                }, responseExecutors);

                // ответ
                return responseBuffer;
            } catch (RequestBuildException | IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
