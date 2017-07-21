package com.myccnice.util;

public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    private ConfigManager() {

    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public String getValue(Class<?> clazz, int att) {
        return null;
    }
}
