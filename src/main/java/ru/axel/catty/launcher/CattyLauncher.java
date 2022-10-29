package ru.axel.catty.launcher;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class CattyLauncher {
    public static @NotNull IHttpBuilder builder(Logger logger) {
        return new HttpBuilder(logger);
    }
}
