package cc.irori.watchdog;

import cc.irori.watchdog.util.Logs;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.concurrent.atomic.AtomicLong;

public class ServerWatchdog {

    private static final HytaleLogger LOGGER = Logs.logger();

    private final AtomicLong lastResponse = new AtomicLong(System.currentTimeMillis());

    private Thread watchdogThread;
    private World lastDefaultWorld;

    public ServerWatchdog() {
        lastDefaultWorld = Universe.get().getDefaultWorld();
        start();
    }

    private void start() {
        LOGGER.atInfo().log("Starting server watchdog (default world: " + lastDefaultWorld.getName() + ")");
        watchdogThread = new Thread(this::runWatchdog, "Irori-Server-Watchdog");
        watchdogThread.setDaemon(true);
        watchdogThread.start();
    }

    public void shutdown() {
        LOGGER.atInfo().log("Stopping server watchdog");
        watchdogThread.interrupt();
    }

    private void runWatchdog() {
        WatchdogConfig config = WatchdogPlugin.getInstance().getConfig().get();

        try {
            LOGGER.atInfo().log("Watchdog will activate in " + config.activateSeconds + " seconds");
            Thread.sleep(config.activateSeconds * 1000L);

            LOGGER.atInfo().log("Watchdog running");
            while (true) {
                watchForAutoRestartingWorlds(config);
                watchForServerShutdown(config);
                watchForDefaultWorld(config);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable t) {
            LOGGER.atSevere().withCause(t).log("Watchdog encountered an error, restarting");
            start();
        }
    }

    private void watchForAutoRestartingWorlds(WatchdogConfig config) throws InterruptedException {
        for (String worldName : config.autoRestartingWorlds) {
            World world = Universe.get().getWorld(worldName);
            if (world == null || !world.isAlive()) {
                if (!Universe.get().isWorldLoadable(worldName)) {
                    if (!config.suppressInvalidWorldWarnings) {
                        LOGGER.atWarning().log("World " + worldName + " is not loadable");
                    }
                    continue;
                }

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
                lastResponse.set(System.currentTimeMillis());
            });
        } catch (Exception e) {
            shutdown = true;
            shutdownReason = "World " + world.getName() + " was unable to accept tasks. The world may have been crashed.";
        }

        checkAndShutdown(config, shutdownReason, shutdown);

        Thread.sleep(5000);
        long elapsed = System.currentTimeMillis() - lastResponse.get();

        if (elapsed > config.watchTimeoutSeconds * 1000L) {
            shutdown = true;
            shutdownReason = "World" + world.getName() + " did not respond for " + (elapsed / 1000) + " seconds.";
        }

        checkAndShutdown(config, shutdownReason, shutdown);
    }

    private void checkAndShutdown(WatchdogConfig config, String reason, boolean shutdown) throws InterruptedException {
        if (shutdown) {
            triggerWatchdog(config, reason);
        }
    }

    private void triggerWatchdog(WatchdogConfig config, String reason) throws InterruptedException {
        LOGGER.atSevere().log("========== WATCHDOG DETECTED A STUCK ==========");
        LOGGER.atSevere().log("Reason: " + reason);
        LOGGER.atSevere().log("Dumping threads and shutting down the server...");

        Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
            if (config.dumpAllThreads || thread.getName().startsWith("WorldThread")) {
                LOGGER.atSevere().log("Thread: " + thread.getName() + " (ID: " + thread.getId() + "):");
                for (StackTraceElement element : stackTrace) {
                    LOGGER.atSevere().log("    at " + element.toString());
                }
            }
        });

        Thread.sleep(5000);
        HytaleServer.get().shutdownServer(ShutdownReason.CRASH.withMessage("Watchdog triggered a shutdown"));
        handleShutdownTimeout(config);
    }

    private void handleShutdownTimeout(WatchdogConfig config) throws InterruptedException {
        Thread.sleep(config.shutdownTimeoutSeconds * 1000L);
        LOGGER.atSevere().log("Shutdown cannot proceed. Forcing exit.");
        Runtime.getRuntime().halt(1);
    }
}
