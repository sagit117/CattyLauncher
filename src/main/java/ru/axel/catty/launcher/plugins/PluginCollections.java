package ru.axel.catty.launcher.plugins;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.launcher.utilities.Pair;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public final class PluginCollections {
    /**
     * Плагин для чтения параметров формы.
     * @return метод плагина.
     */
    @Contract(pure = true)
    public static @NotNull RouteExecute formParametersParser(Logger logger) {
        return (request, response) -> {
            if (Objects.equals(request.getHeaders(Headers.CONTENT_TYPE), "application/x-www-form-urlencoded")) {
                final var splitBody = request.getBody().split("&");

                Arrays.stream(splitBody).forEach(parameters -> {
                    final Pair<String, String> pair = new Pair<>(parameters.split("="));

                    logger.finest(
                      "Установка параметров в запрос: " + pair
                    );

                    if (pair.getValue() != null) {
                        request.setParams(
                            pair.getKey(),
                            URLDecoder.decode(pair.getValue(), StandardCharsets.UTF_8)
                        );
                    }
                });
            }
        };
    }
}
