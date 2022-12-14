package ru.axel.catty.launcher;

import ru.axel.catty.engine.plugins.Plugins;
import ru.axel.catty.engine.routing.IRouting;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.launcher.config.IConfig;

import java.lang.reflect.InvocationTargetException;

/**
 * Построитель конвейера обработки запроса/ответа
 */
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

    /**
     * Метод применится после предоставления ответа
     * @param execute обработчик
     * @return построитель
     */
    IHttpBuilder useAfterResponse(RouteExecute execute);

    IHttpBuilder useWithExceptionally(RouteExecute execute);

    void launch();
    IConfig getConfig();
}
