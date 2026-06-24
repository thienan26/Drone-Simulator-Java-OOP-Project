package dronesim.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures the root java.util.logging logger to write to dronesim.log.
 * All classes obtain their own Logger via Logger.getLogger(ClassName.class.getName()).
 */
public final class AppLogger {

    private AppLogger() { }

    /**
     * Sets up file-based logging at INFO level.
     * Must be called once at application startup before any other code runs.
     */
    public static void setup() {
        try {
            Logger rootLogger = Logger.getLogger("");
            // Remove default console handler to avoid duplicate output
            java.util.logging.Handler[] handlers = rootLogger.getHandlers();
            for (java.util.logging.Handler h : handlers) {
                rootLogger.removeHandler(h);
            }

            FileHandler fileHandler = new FileHandler("dronesim.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);

            Logger.getLogger(AppLogger.class.getName()).info("Logger initialised – writing to dronesim.log");
        } catch (IOException e) {
            System.err.println("Logger setup failed: " + e.getMessage());
        }
    }
}
