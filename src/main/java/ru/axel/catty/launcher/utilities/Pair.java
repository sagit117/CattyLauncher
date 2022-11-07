package ru.axel.catty.launcher.utilities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public Pair(String @NotNull [] arr) {
        key = (K) arr[0];
        value = arr[1] != null ? (V) arr[1] : null;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Contract(pure = true)
    public @NotNull String toString() {
        return "[key=" + key + ", value=" + value + "]";
    }
}
