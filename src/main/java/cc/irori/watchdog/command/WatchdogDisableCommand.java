package cc.irori.watchdog.command;

import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogDisableCommand extends CommandBase {

    public WatchdogDisableCommand() {
        super("disable", "watchdog.commands.watchdog.disable.desc");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        if (plugin.stopWatchdog(true)) {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.disable.success"));
        } else {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.disable.alreadyDisabled"));
        }
    }
}
