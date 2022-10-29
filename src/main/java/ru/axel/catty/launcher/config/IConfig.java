package ru.axel.catty.launcher.config;

import com.typesafe.config.Config;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public interface IConfig {
    InetSocketAddress getHostAddress();
    int getPoolLimit();
    int getLimitAllocateBufferForRequest();
    int getPort();
    int getAnswerTimeout();
    String getProtocol();

    /**
     * Метод позволяет читать настройки и возвращать в случае ошибки значение по умолчанию.
     *
     * @param prop      строка для поиска значения в файле настроек.
     * @param orDefault значение по умолчанию.
     * @param <T>       тип который должен вернуть метод
     * @return либо значение настроек, либо значение по умолчанию.
     */
    @SuppressWarnings("unchecked")
    default <T> T getConfigOrDefault(String prop, T orDefault, Config config) {
        try {
            if (orDefault instanceof Integer) {
                return (T) Integer.valueOf(config.getInt(prop));
            }
            if (orDefault instanceof String) {
                return (T) config.getString(prop);
            }

            return orDefault;
        } catch (Exception e) {
            return orDefault;
        }
    }
}
