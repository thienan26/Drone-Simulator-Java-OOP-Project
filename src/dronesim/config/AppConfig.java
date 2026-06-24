package dronesim.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads and persists the API base URL and authentication token using a properties file.
 * Satisfies the streams-and-files requirement.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_URL = "https://dronesim.facets-labs.com";

    private String baseUrl;
    private String token;

    public AppConfig(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    /**
     * Reads config.properties from the working directory.
     * Returns a default instance when no file exists yet.
     */
    public static AppConfig load() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            LOGGER.info("Configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.info("No config file found – using defaults. A new one will be created on save.");
        }
        return new AppConfig(
                props.getProperty("baseUrl", DEFAULT_URL),
                props.getProperty("token", "")
        );
    }

    /**
     * Writes the current base URL and token to config.properties.
     */
    public void save() {
        Properties props = new Properties();
        props.setProperty("baseUrl", baseUrl);
        props.setProperty("token", token);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Drone Simulation Settings");
            LOGGER.info("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.warning("Could not save configuration: " + e.getMessage());
        }
    }

    /** Returns true when both baseUrl and token are non-empty. */
    public boolean isValid() {
        return baseUrl != null && !baseUrl.isBlank()
                && token != null && !token.isBlank();
    }

    public String getBaseUrl() { return baseUrl; }
    public String getToken() { return token; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setToken(String token) { this.token = token; }
}
