package cc.irori.watchdog;

import cc.irori.watchdog.command.WatchdogCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;

public class WatchdogPlugin extends JavaPlugin {

    private static WatchdogPlugin instance;

    private final Config<WatchdogConfig> config;

    private ServerWatchdog watchdog;

    public WatchdogPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;

        this.config = withConfig(WatchdogConfig.CODEC);
    }

    @Override
    protected void start() {
        config.load().join();
        if (config.get().enabled) {
            startWatchdog(false);
        }

        getCommandRegistry().registerCommand(new WatchdogCommand());
    }

    @Override
    protected void shutdown() {
        stopWatchdog(false);
    }

    public Config<WatchdogConfig> getConfig() {
        return config;
    }

    public boolean startWatchdog(boolean writeConfig) {
        if (watchdog == null) {
            watchdog = new ServerWatchdog();
            if (writeConfig) {
                config.get().enabled = true;
                config.save();
            }
            return true;
        }
        return false;
    }

    public boolean stopWatchdog(boolean writeConfig) {
        if (watchdog != null) {
            watchdog.shutdown();
            watchdog = null;
            if (writeConfig) {
                config.get().enabled = false;
                config.save();
            }
            return true;
        }
        return false;
    }

    public @Nullable ServerWatchdog getWatchdog() {
        return watchdog;
    }

    public static WatchdogPlugin getInstance() {
        return instance;
    }
}
