package cc.irori.watchdog.command.world;

import cc.irori.watchdog.WatchdogConfig;
import cc.irori.watchdog.WatchdogPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WatchdogWorldRemoveCommand extends CommandBase {

    private final RequiredArg<String> worldNameArg;

    public WatchdogWorldRemoveCommand() {
        super("remove", "watchdog.commands.watchdog.world.remove.desc");
        worldNameArg = withRequiredArg("world", "watchdog.commands.watchdog.world.remove.world.desc", ArgTypes.STRING);
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        WatchdogPlugin plugin = WatchdogPlugin.getInstance();
        WatchdogConfig config = plugin.getConfig().get();
        String worldName = worldNameArg.get(context);

        String[] currentWorlds = config.autoRestartingWorlds;
        for (int i = 0; i < currentWorlds.length; i++) {
            if (currentWorlds[i].equalsIgnoreCase(worldName)) {
                String[] newWorlds = new String[currentWorlds.length - 1];
                System.arraycopy(currentWorlds, 0, newWorlds, 0, i);
                System.arraycopy(currentWorlds, i + 1, newWorlds, i, currentWorlds.length - i - 1);
                config.autoRestartingWorlds = newWorlds;
                plugin.getConfig().save();

                context.sendMessage(
                        Message.translation("watchdog.commands.watchdog.world.remove.success")
                                .param("world", worldName)
                );
                return;
            }
        }
        context.sendMessage(Message.translation("watchdog.commands.watchdog.world.remove.notFound"));
    }
}
