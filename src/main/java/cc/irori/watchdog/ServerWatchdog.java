package cc.irori.watchdog;

import cc.irori.watchdog.util.Logs;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerWatchdog {

    private static final long WORLD_RESPONSE_ERROR = -1L;

    private static final HytaleLogger LOGGER = Logs.logger();

    private final AtomicLong defaultWorldResponse = new AtomicLong(System.currentTimeMillis());
    private final Map<String, Long> worldResponseMap = new ConcurrentHashMap<>();

    private Thread watchdogThread;
    private World lastDefaultWorld;

    private volatile State state = State.ACTIVATING;

    public ServerWatchdog() {
        lastDefaultWorld = Universe.get().getDefaultWorld();
        start();
    }

    public State getState() {
        return state;
    }

    public void shutdown() {
        LOGGER.atInfo().log("Stopping server watchdog");
        watchdogThread.interrupt();
    }

    private void start() {
        LOGGER.atInfo().log("Starting server watchdog (default world: " + lastDefaultWorld.getName() + ")");
        watchdogThread = new Thread(this::runWatchdog, "Irori-Server-Watchdog");
        watchdogThread.setDaemon(true);
        watchdogThread.start();
    }

    private void runWatchdog() {
        WatchdogConfig config = WatchdogPlugin.getInstance().getConfig().get();
        state = State.ACTIVATING;

        try {
            LOGGER.atInfo().log("Watchdog will activate in " + config.activateSeconds + " seconds");
            Thread.sleep(config.activateSeconds * 1000L);

            state = State.RUNNING;
            LOGGER.atInfo().log("Watchdog running");
            while (true) {
                watchForServerShutdown(config);
                requestWorldResponse(config);
                watchForDefaultWorld(config);
                watchForAutoRestartingWorlds(config);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable t) {
            LOGGER.atSevere().withCause(t).log("Watchdog encountered an error, restarting");
            start();
        }
    }

    private void requestWorldResponse(WatchdogConfig config) throws InterruptedException {
        for (String worldName : config.autoRestartingWorlds) {
            World world = Universe.get().getWorld(worldName);
            if (world == null) {
                continue;
            }

            worldResponseMap.computeIfAbsent(worldName, key -> WORLD_RESPONSE_ERROR);
            try {
                world.execute(() -> worldResponseMap.put(worldName, System.currentTimeMillis()));
            } catch (Exception ignored) {}
        }
    }

    private void watchForAutoRestartingWorlds(WatchdogConfig config) throws InterruptedException {
        for (String worldName : config.autoRestartingWorlds) {
            World world = Universe.get().getWorld(worldName);
            Long response = worldResponseMap.get(worldName);

            boolean restart = false;
            if (world == null || !world.isAlive()) {
                restart = true;
            } else if (response != null && response == WORLD_RESPONSE_ERROR) {
                LOGGER.atSevere().log("World " + worldName + " was unable to accept tasks. The world may have been crashed.");
                restart = true;
            } else if (response != null) {
                long elapsed = System.currentTimeMillis() - response;
                if (elapsed > config.watchTimeoutSeconds * 1000L) {
                    LOGGER.atSevere().log("World " + worldName + " did not respond for " + (elapsed / 1000) + " seconds.");
                    restart = true;
                }
            }

            if (restart) {
                worldResponseMap.remove(worldName);
                if (!Universe.get().isWorldLoadable(worldName)) {
                    if (!config.suppressInvalidWorldWarnings) {
                        LOGGER.atWarning().log("World " + worldName + " is not loadable");
                    }
                    continue;
                }

                LOGGER.atSevere().log("========== WORLD STUCK DETECTED ==========");
                dumpThreads(config);

                LOGGER.atInfo().log("Attempting to unload world: " + worldName);
                try {
                    Universe.get().removeWorld(worldName);
                } catch (NullPointerException ignored) {}

                LOGGER.atInfo().log("Restarting world: " + worldName);
                try {
                    Universe.get().loadWorld(worldName).join();
                    LOGGER.atInfo().log("World " + worldName + " loaded");
                } catch (Exception e) {
                    LOGGER.atSevere().withCause(e).log("Failed to load world: " + worldName);
                }
            }
        }
    }

    private void watchForServerShutdown(WatchdogConfig config) throws InterruptedException {
        if (HytaleServer.get().isShuttingDown()) {
            LOGGER.atInfo().log("Server shutdown detected");
            handleShutdownTimeout(config);
        }
    }

    private void watchForDefaultWorld(WatchdogConfig config) throws InterruptedException {
        boolean shutdown = false;
        String shutdownReason = "Unknown";

        World world = Universe.get().getDefaultWorld();
        if (world == null || !world.isAlive()) {
            shutdown = true;
            shutdownReason = "Default world " + (world != null ? world.getName() + " " : "") + "is not alive.";
        } else if (lastDefaultWorld != world) {
            LOGGER.atInfo().log("Default world changed to " + world.getName() + " (" + world.hashCode() + ")");
            lastDefaultWorld = world;
        }

        checkAndShutdown(config, shutdownReason, shutdown);

        try {
            world.execute(() -> {
                defaultWorldResponse.set(System.currentTimeMillis());
            });
        } catch (Exception e) {
            shutdown = true;
            shutdownReason = "World " + world.getName() + " was unable to accept tasks. The world may have been crashed.";
        }

        checkAndShutdown(config, shutdownReason, shutdown);

        Thread.sleep(5000);
        long elapsed = System.currentTimeMillis() - defaultWorldResponse.get();

        if (elapsed > config.watchTimeoutSeconds * 1000L) {
            shutdown = true;
            shutdownReason = "World " + world.getName() + " did not respond for " + (elapsed / 1000) + " seconds.";
        }

        checkAndShutdown(config, shutdownReason, shutdown);
    }

    private void checkAndShutdown(WatchdogConfig config, String reason, boolean shutdown) throws InterruptedException {
        if (shutdown) {
            triggerWatchdog(config, reason);
        }
    }

    private void triggerWatchdog(WatchdogConfig config, String reason) throws InterruptedException {
        LOGGER.atSevere().log("========== SERVER STUCK DETECTED ==========");
        LOGGER.atSevere().log("Reason: " + reason);
        LOGGER.atSevere().log("Dumping threads and shutting down the server...");

        dumpThreads(config);

        Thread.sleep(5000);
        HytaleServer.get().shutdownServer(ShutdownReason.CRASH.withMessage("Watchdog triggered a shutdown"));
        handleShutdownTimeout(config);
    }

    private void dumpThreads(WatchdogConfig config) {
        Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
            if (config.dumpAllThreads || thread.getName().startsWith("WorldThread")) {
                LOGGER.atSevere().log("Thread: " + thread.getName() + " (ID: " + thread.getId() + "):");
                for (StackTraceElement element : stackTrace) {
                    LOGGER.atSevere().log("    at " + element.toString());
                }
            }
        });
    }

    private void handleShutdownTimeout(WatchdogConfig config) throws InterruptedException {
        Thread.sleep(config.shutdownTimeoutSeconds * 1000L);
        LOGGER.atSevere().log("Shutdown cannot proceed. Forcing exit.");
        Runtime.getRuntime().halt(1);
    }

    public enum State {
        ACTIVATING,
        RUNNING
    }
}
