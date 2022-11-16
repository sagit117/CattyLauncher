package ru.axel;

import org.jetbrains.annotations.NotNull;
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
import ru.axel.logger.MiniLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing(logger);
    private static final Plugins plugins = new Plugins(logger);

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        new TestRoute(routing);

        CattyLauncher
            .builder(logger)
            .setConfig(ConfigApp.class, "config/application.conf")
            .usePlugins(plugins)
            .useRouting(routing)
            .useWithExceptionally((iHttpCattyRequest, iHttpCattyResponse) -> {
                logger.info("Клиент: " + iHttpCattyRequest.getClientInfo().toString());
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
            response.respond(ResponseCode.OK, "HOME OK");
        }

        @GET(path = "/")
        public void homeLittle(IHttpCattyRequest request, @NotNull IHttpCattyResponse response) {
            response.respond(ResponseCode.OK, "HOME OK");
        }
    }
}