package ru.axel;

import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.Routing;
import ru.axel.catty.launcher.CattyLauncher;
import ru.axel.catty.launcher.config.ConfigApp;
import ru.axel.logger.MiniLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = MiniLogger.getLogger(Main.class);
    private static final IRouting routing = new Routing(logger);
    private static final Plugins plugins = new Plugins(logger);

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CattyLauncher
            .builder(logger)
            .setConfig(ConfigApp.class, "config/application.conf")
            .setPlugins(plugins)
            .setRouting(routing)
            .launch();
    }
}