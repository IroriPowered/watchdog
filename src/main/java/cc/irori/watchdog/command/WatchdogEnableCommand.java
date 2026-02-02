package cc.irori.watchdog.command;

import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogEnableCommand extends CommandBase {

    public WatchdogEnableCommand() {
        super("enable", "watchdog.commands.watchdog.enable.desc");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        if (plugin.startWatchdog(true)) {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.enable.success"));
        } else {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.enable.alreadyEnabled"));
        }
    }
}
