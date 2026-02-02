package cc.irori.watchdog.command.world;

import cc.irori.watchdog.WatchdogConfig;
import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogWorldListCommand extends CommandBase {

    public WatchdogWorldListCommand() {
        super("list", "watchdog.commands.watchdog.world.list.desc");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        WatchdogConfig config = plugin.getConfig().get();
        String[] currentWorlds = config.autoRestartingWorlds;

        if (currentWorlds.length == 0) {
            context.sendMessage(Message.translation("watchdog.commands.watchdog.world.list.noWorlds"));
            return;
        }

        StringBuilder worldsList = new StringBuilder();
        for (String world : currentWorlds) {
            worldsList.append("- ").append(world).append("\n");
        }
        context.sendMessage(
                Message.join(
                        Message.translation("watchdog.commands.watchdog.world.list.success"),
                        Message.raw("\n"),
                        Message.raw(worldsList.toString().trim())
                )
        );
    }
}
