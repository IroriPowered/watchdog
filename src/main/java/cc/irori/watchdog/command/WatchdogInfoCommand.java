package cc.irori.watchdog.command;

import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogInfoCommand extends CommandBase {

    public WatchdogInfoCommand() {
        super("info", "watchdog.commands.watchdog.info.desc");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        context.sendMessage(Message.join(
                Message.raw("Watchdog " + plugin.getManifest().getVersion().toString() + " by IroriPowered")
        ));
    }
}
