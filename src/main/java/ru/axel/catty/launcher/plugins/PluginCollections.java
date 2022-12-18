package ru.axel.catty.launcher.plugins;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.axel.catty.engine.headers.Headers;
import ru.axel.catty.engine.response.TransformResponse;
import ru.axel.catty.engine.routing.RouteExecute;
import ru.axel.catty.launcher.utilities.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

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

                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Установка параметров в запрос: " + pair);
                    }

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

    @Contract(pure = true)
    public static @NotNull RouteExecute Gzip(Logger logger) {
        return (request, response) -> {
            final String encoding = request.getHeaders(Headers.ACCEPT_ENCODING);

            if (encoding != null) {
                final List<String> list = Arrays.stream(encoding.split(",")).map(String::toLowerCase).toList();

                final boolean isGzip = list.contains("gzip");

                if (isGzip) {
                    response.setTransformMethod((byteResponse) -> {
                        if (byteResponse.length > 1024) {
                            try (
                                final ByteArrayOutputStream bos = new ByteArrayOutputStream(byteResponse.length);
                                final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos)
                            ) {
                                gzipOutputStream.write(byteResponse);
                                gzipOutputStream.finish();

                                response.addHeader(Headers.CONTENT_ENCODING, "gzip");

                                final byte[] result = bos.toByteArray();

                                if (logger.isLoggable(Level.FINEST)) {
                                    logger.finest("Сжатие данных: " + byteResponse.length + " --> " + result.length);
                                }

                                return result;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            return byteResponse;
                        }
                    });
                }
            }
        };
    }
}
