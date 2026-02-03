package cc.irori.watchdog.command;

import cc.irori.watchdog.command.world.WatchdogWorldCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WatchdogCommand extends AbstractCommandCollection {

    public WatchdogCommand() {
        super("watchdog", "watchdog.commands.watchdog.desc");
        addSubCommand(new WatchdogStateCommand());
        addSubCommand(new WatchdogEnableCommand());
        addSubCommand(new WatchdogDisableCommand());
        addSubCommand(new WatchdogWorldCommand());
    }
}
