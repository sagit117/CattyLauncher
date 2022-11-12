package ru.axel.catty.launcher.factory;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.Route;
import ru.axel.catty.engine.routing.RouteExecute;

/**
 * Класс фабрика для создания маршрутов
 */
final public class RouteFactory {
    public static @NotNull ICattyRoute get(String path, RouteExecute execute) {
        return new Route(path, "GET", execute);
    }

    public static @NotNull ICattyRoute post(String path, RouteExecute execute) {
        return new Route(path, "POST", execute);
    }

    public static @NotNull ICattyRoute options(String path, RouteExecute execute) {
        return new Route(path, "OPTIONS", execute);
    }
}
