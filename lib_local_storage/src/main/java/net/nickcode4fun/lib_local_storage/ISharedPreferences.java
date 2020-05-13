package net.nickcode4fun.lib_local_storage;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public interface ISharedPreferences {
    /**
     * Get data.
     *
     * @param key          key of value
     * @param defaultValue If key not found return default value.
     * @return value.
     */
    <T> T get(@NonNull String key, @NonNull T defaultValue);

    <T> List<T> getAllValuesOfKey(String key);

    Map<String, ?> getAll();

    <T> void put(@NonNull String key, @NonNull T value);

    <T> void putAllValuesOfKey(@NonNull String key, @NonNull List<T> values);

    boolean containsKey(@NonNull String key);

    void remove(@NonNull String key);

    void clear();
}
