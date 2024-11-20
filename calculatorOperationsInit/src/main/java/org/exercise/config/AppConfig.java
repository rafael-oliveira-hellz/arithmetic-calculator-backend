package org.exercise.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    static {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("DB_HOST", dotenv.get("DB_HOST", "localhost"));
        System.setProperty("DB_PORT", dotenv.get("DB_PORT", "5432"));
        System.setProperty("DB_NAME", dotenv.get("DB_NAME", "default_db"));
        System.setProperty("DB_USER", dotenv.get("DB_USER", "default_user"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD", "default_password"));
    }
}
