package cc.irori.watchdog.command.world;

import cc.irori.watchdog.WatchdogConfig;
import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogWorldAddCommand extends CommandBase {

    private final RequiredArg<String> worldNameArg;

    public WatchdogWorldAddCommand() {
        super("add", "watchdog.commands.watchdog.world.add.desc");
        worldNameArg = withRequiredArg("world", "watchdog.commands.watchdog.world.add.world.desc", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        WatchdogConfig config = plugin.getConfig().get();
        String worldName = worldNameArg.get(context);

        String[] currentWorlds = config.autoRestartingWorlds;
        for (String world : currentWorlds) {
            if (world.equalsIgnoreCase(worldName)) {
                context.sendMessage(Message.translation("watchdog.commands.watchdog.world.add.alreadyExists"));
                return;
            }
        }

        String[] newWorlds = new String[currentWorlds.length + 1];
        System.arraycopy(currentWorlds, 0, newWorlds, 0, currentWorlds.length);
        newWorlds[currentWorlds.length] = worldName;
        config.autoRestartingWorlds = newWorlds;
        plugin.getConfig().save();

        context.sendMessage(
                Message.translation("watchdog.commands.watchdog.world.add.success")
                        .param("world", worldName)
        );
    }
}
