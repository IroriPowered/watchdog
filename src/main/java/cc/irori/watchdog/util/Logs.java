package cc.irori.watchdog.util;

import com.hypixel.hytale.logger.HytaleLogger;

public class Logs {

    private static final String LOGGER_NAME = "Watchdog";

    public static HytaleLogger logger() {
        return HytaleLogger.get(LOGGER_NAME);
    }
}
