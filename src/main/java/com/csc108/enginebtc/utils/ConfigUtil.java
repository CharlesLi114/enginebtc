package com.csc108.enginebtc.utils;


public class ConfigUtil {


    public static String getPropertyConfigFile() {
        String property_file_name = System.getProperty("properties.name");
        if (property_file_name == null || property_file_name.equalsIgnoreCase("null")) {
            property_file_name = "dev/application.properties";
        }
        return getConfigPath(property_file_name);
    }

    public static String getConfigPath(String configFile) {
        String env = System.getProperty("ENV");
        if (env == null) {
            env = "dev";
        }
        configFile = env + "/" + configFile;
        
        return configFile;
    }
    
    public static String getLogConfigPath(String configFile) {
        String env = System.getProperty("ENV");
        if (env == null) {
            env = "dev";
        }
        configFile = env + "/" + configFile;
        
        return configFile;
    }
}
