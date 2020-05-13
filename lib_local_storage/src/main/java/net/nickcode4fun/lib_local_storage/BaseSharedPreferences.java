package net.nickcode4fun.lib_local_storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BaseSharedPreferences implements ISharedPreferences {

    private SharedPreferences preferences;

    public BaseSharedPreferences(@NonNull Context context, @NonNull String fileName) {
        this(context, fileName, Context.MODE_PRIVATE);
    }

    public BaseSharedPreferences(@NonNull Context context, @NonNull String fileName, int mode) {
        this.preferences = context.getSharedPreferences(fileName, mode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@NonNull String key, @NonNull T defaultValue) {
        String value = preferences.getString(key, String.valueOf(defaultValue));

        if (value == null) {
            return null;
        } else if (defaultValue instanceof String) {
            return (T) value;
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(value);
        } else if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(value);
        } else if (defaultValue instanceof Float) {
            return (T) Float.valueOf(value);
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(value);
        } else {
            throw new IllegalArgumentException("value class is not base type of \'java.lang\' class! ex: (String/Boolean/Integer...)");
        }
    }

    @Override
    public <T> List<T> getAllValuesOfKey(String key) {

        String json = this.get(key, "");

        if (json == null || json.isEmpty())
            return null;

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<T>>() {
            }.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    @Override
    public <T> void put(@NonNull String key, @NonNull T value) {
        if (value instanceof String) {
            preferences.edit().putString(key, (String) value).apply();
        } else if (value instanceof Integer) {
            preferences.edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof Boolean) {
            preferences.edit().putBoolean(key, (Boolean) value).apply();
        } else if (value instanceof Float) {
            preferences.edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Long) {
            preferences.edit().putLong(key, (Long) value).apply();
        } else {
            throw new IllegalArgumentException("value class is not base type of \'java.lang\' class! ex: (String/Boolean/Integer...)");
        }
    }

    @Override
    public <T> void putAllValuesOfKey(@NonNull String key, @NonNull List<T> values) {
        Gson gson = new Gson();
        String json = gson.toJson(values);
        this.put(key, json);
    }

    @Override
    public boolean containsKey(@NonNull String key) {
        return preferences.contains(key);
    }

    @Override
    public void remove(@NonNull String key) {
        preferences.edit().remove(key).apply();
    }

    @Override
    public void clear() {
        preferences.edit().clear().apply();
    }
}
