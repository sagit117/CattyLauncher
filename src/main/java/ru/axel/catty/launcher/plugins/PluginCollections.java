package ru.axel.catty.launcher.plugins;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.routing.RouteExecute;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class PluginCollections {
    /**
     * Плагин для чтения параметров формы.
     * @return метод плагина.
     */
    @Contract(pure = true)
    public static @NotNull RouteExecute formParametersParser() {
        return (request, response) -> {
            if (Objects.equals(request.getHeaders(Headers.CONTENT_TYPE), "application/x-www-form-urlencoded")) {
                final var splitBody = request.getBody().split("&");

                Arrays.stream(splitBody).forEach(parameters -> {
                    final var splitParameters = parameters.split("=");

                    request.setParams(
                        splitParameters[0],
                        splitParameters.length > 1
                            ? URLDecoder.decode(splitParameters[1], StandardCharsets.UTF_8)
                            : null
                    );
                });
            }
        };
    }
}
