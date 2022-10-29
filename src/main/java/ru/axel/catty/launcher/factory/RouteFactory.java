package ru.axel.catty.launcher.factory;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.routing.ICattyRoute;
import ru.axel.catty.engine.routing.Route;
import ru.axel.catty.engine.routing.RouteExecute;

final public class RouteFactory {
    @Contract("_, _ -> new")
    public static @NotNull ICattyRoute get(String path, RouteExecute execute) {
        return new Route(path, "GET", execute);
    }
    public static @NotNull ICattyRoute post(String path, RouteExecute execute) {
        return new Route(path, "POST", execute);
    }
}
