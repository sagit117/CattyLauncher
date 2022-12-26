import org.jetbrains.annotations.NotNull;
import ru.axel.Main;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.request.IHttpCattyRequest;
import ru.axel.catty.engine.response.IHttpCattyResponse;
import ru.axel.catty.engine.response.ResponseCode;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.Routing;
import ru.axel.catty.launcher.CattyLauncher;
import ru.axel.catty.launcher.annotations.GET;
import ru.axel.catty.launcher.config.ConfigApp;
import ru.axel.catty.launcher.controllers.BaseController;
import ru.axel.catty.launcher.plugins.PluginCollections;
import ru.axel.logger.MiniLogger;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTest {
    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing(logger);
    private static final Plugins plugins = new Plugins(logger);

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        routing.staticResourceFiles("/static");

        new TestRoute(routing);

        plugins.addPipelines("gzip", PluginCollections.Gzip(logger));

        CattyLauncher
            .builder(logger)
            .setConfig(ConfigApp.class, "config/application.conf")
            .usePlugins(plugins)
            .useRouting(routing)
                .useWithExceptionally((request, response) -> {
                    final Logger logger = request.getLogger();

                    if (request.getExceptionList().size() > 0) {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Запрос содержит ошибки. Количество ошибок: " + request.getExceptionList().size());
                        }

                        request.getExceptionList().forEach(exception -> {
                            if (logger.isLoggable(Level.FINEST)) {
                                logger.finest("Класс ошибки: " + exception.getClass().getName());
                                logger.finest("Сообщение об ошибке: " + exception.getMessage());

                                exception.printStackTrace();
                            }
                        });
                    }

                    /* Выводим статику на ошибки запроса */
                    if (response.getResponseCode() == 404) {
                        try (final InputStream notFound = Main.class.getResourceAsStream("/static/templates/NotFound.html")) {
                            assert notFound != null;
                            response.setBody(notFound.readAllBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (response.getResponseCode() == 500) {
                        try (final InputStream internalServerError = Main.class.getResourceAsStream("/static/templates/InternalServerError.html")) {
                            assert internalServerError != null;
                            response.setBody(internalServerError.readAllBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
            .useAfterResponse((iHttpCattyRequest, iHttpCattyResponse) -> {
                logger.info("Код ответ: " + iHttpCattyResponse.getResponseCode());
            })
            .launch();
    }

    public static class TestRoute extends BaseController {
        TestRoute(IRouting routing) {
            super(routing);
        }

        @GET(path = "/home")
        public void home(IHttpCattyRequest request, @NotNull IHttpCattyResponse response) {
            final String body = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Status</title>
                    <link rel="stylesheet" type="text/css" href="/static/styles/index.css">
    
                </head>
                <body>
                    <h1>Тестовая страница</h1>
                    <form method="post" enctype="multipart/form-data">
                        <input type="file" name="file" multiple>
                        <button type="submit">SUBMIT</button>
                    </form>
                </body>
            """;

            response.addHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
            response.respond(ResponseCode.OK, body);
        }

        @GET(path = "/")
        public void homeLittle(IHttpCattyRequest request, @NotNull IHttpCattyResponse response) {
            response.respond(ResponseCode.OK, "HOME OK");
        }
    }
}
