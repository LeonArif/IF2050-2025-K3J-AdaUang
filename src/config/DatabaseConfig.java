package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Properties properties;
    
    private DatabaseConfig() {
        loadProperties();
    }
    
    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try {
            // Try to load from config file first
            FileInputStream file = new FileInputStream("config/database.properties");
            properties.load(file);
            file.close();
        } catch (IOException e) {
            // Fallback to default values if config file not found
            System.out.println("Config file not found, using default values");
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        // Default fallback values
        properties.setProperty("db.host", "127.0.0.1");
        properties.setProperty("db.port", "3306");
        properties.setProperty("db.name", "adauang_db");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "<<YOUR_PASSWORD_HERE>>");
        properties.setProperty("db.table.users", "users");
        properties.setProperty("app.name", "AdaUang");
        properties.setProperty("debug.mode", "true");
    }
    
    // Getter
    public String getDbUrl() {
        return String.format("jdbc:mysql://%s:%s/%s", 
            getProperty("db.host"), 
            getProperty("db.port"), 
            getProperty("db.name"));
    }
    
    public String getDbUsername() {
        return getProperty("db.username");
    }
    
    public String getDbPassword() {
        return getProperty("db.password");
    }
    
    public String getUsersTableName() {
        return getProperty("db.table.users");
    }
    
    public String getAppName() {
        return getProperty("app.name");
    }
    
    public boolean isDebugMode() {
        return Boolean.parseBoolean(getProperty("debug.mode"));
    }
    
    private String getProperty(String key) {
        return properties.getProperty(key, "");
    }
    
    // Test connection method
    public void printConfig() {
        if (isDebugMode()) {
            System.out.println("=== DATABASE CONFIGURATION ===");
            System.out.println("URL: " + getDbUrl());
            System.out.println("Username: " + getDbUsername());
            System.out.println("Password: " + (getDbPassword().isEmpty() ? "(empty)" : "***"));
            System.out.println("Users Table: " + getUsersTableName());
            System.out.println("App Name: " + getAppName());
            System.out.println("===============================");
        }
    }
}