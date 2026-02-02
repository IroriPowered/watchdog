package cc.irori.watchdog.command.world;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WatchdogWorldCommand extends AbstractCommandCollection {

    public WatchdogWorldCommand() {
        super("world", "watchdog.commands.watchdog.world.desc");
        addSubCommand(new WatchdogWorldAddCommand());
        addSubCommand(new WatchdogWorldRemoveCommand());
        addSubCommand(new WatchdogWorldListCommand());
    }
}
