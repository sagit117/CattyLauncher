package ru.axel.catty.launcher;

import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.launcher.config.IConfig;

import java.lang.reflect.InvocationTargetException;

public interface IHttpBuilder {
    IHttpBuilder setConfig(
        Class<? extends IConfig> configClass,
        String pathFromResource
    ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
    IHttpBuilder setConfig(
        IConfig config
    );
    IHttpBuilder usePlugins(Plugins plugins);
    IHttpBuilder useRouting(IRouting routing);
    IHttpBuilder useAfterResponse(RouteExecute execute);
    void launch();
    IConfig getConfig();
}
