package cc.irori.watchdog.command;

import cc.irori.watchdog.ServerWatchdog;
import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogStateCommand extends CommandBase {

    public WatchdogStateCommand() {
        super("state", "watchdog.commands.watchdog.state.desc");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        ServerWatchdog watchdog = plugin.getWatchdog();

        if (watchdog == null) {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.state.disabled"));
        } else {
            Message message = switch (watchdog.getState()) {
                case ACTIVATING -> Message.translation("watchdog.commands.watchdog.state.activating");
                case RUNNING -> Message.translation("watchdog.commands.watchdog.state.running");
            };
            context.sendMessage(message);
        }
    }
}
