package cc.irori.watchdog;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class WatchdogConfig {

    public static final BuilderCodec<WatchdogConfig> CODEC = BuilderCodec.builder(WatchdogConfig.class, WatchdogConfig::new)
            .append(
                    new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (config, value, extraInfo) -> config.enabled = value,
                    (config, extraInfo) -> config.enabled
            ).add()
            .append(
                    new KeyedCodec<>("ActivateSeconds", Codec.INTEGER),
                    (config, value, extraInfo) -> config.activateSeconds = value,
                    (config, extraInfo) -> config.activateSeconds
            ).add()
            .append(
                    new KeyedCodec<>("WatchTimeoutSeconds", Codec.INTEGER),
                    (config, value, extraInfo) -> config.watchTimeoutSeconds = value,
                    (config, extraInfo) -> config.watchTimeoutSeconds
            ).add()
            .append(
                    new KeyedCodec<>("ShutdownTimeoutSeconds", Codec.INTEGER),
                    (config, value, extraInfo) -> config.shutdownTimeoutSeconds = value,
                    (config, extraInfo) -> config.shutdownTimeoutSeconds
            ).add()
            .append(
                    new KeyedCodec<>("DumpAllThreads", Codec.BOOLEAN),
                    (config, value, extraInfo) -> config.dumpAllThreads = value,
                    (config, extraInfo) -> config.dumpAllThreads
            ).add()
            .append(
                    new KeyedCodec<>("AutoRestartingWorlds", Codec.STRING_ARRAY),
                    (config, value, extraInfo) -> config.autoRestartingWorlds = value,
                    (config, extraInfo) -> config.autoRestartingWorlds
            ).add()
            .append(
                    new KeyedCodec<>("SuppressInvalidWorldWarnings", Codec.BOOLEAN),
                    (config, value, extraInfo) -> config.suppressInvalidWorldWarnings = value,
                    (config, extraInfo) -> config.suppressInvalidWorldWarnings
            ).add()
            .build();

    public boolean enabled = true;
    public int activateSeconds = 90;
    public int watchTimeoutSeconds = 30;
    public int shutdownTimeoutSeconds = 60;
    public boolean dumpAllThreads = false;
    public String[] autoRestartingWorlds = new String[0];
    public boolean suppressInvalidWorldWarnings = false;
}
