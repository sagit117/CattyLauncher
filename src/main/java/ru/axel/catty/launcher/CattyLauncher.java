package ru.axel.catty.launcher;

import org.jetbrains.annotations.NotNull;
import ru.axel.catty.launcher.config.IConfig;

import java.util.logging.Logger;

public final class CattyLauncher {
    private static IHttpBuilder builder;

    public static @NotNull IHttpBuilder builder(Logger logger) {
        builder = new HttpBuilder(logger);
        return builder;
    }
    public static IConfig getConfig() {
        return builder.getConfig();
    }
}
