package me.neznamy.tab.platforms.bukkit.platform;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.platforms.bukkit.features.PerWorldPlayerList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.Function;

/**
 * Platform override for Folia.
 */
public class FoliaPlatform extends BukkitPlatform {

    /** Global tick thread scheduler */
    @NotNull
    private final GlobalRegionScheduler globalScheduler = Bukkit.getGlobalRegionScheduler();

    /**
     * Constructs new instance with given plugin.
     *
     * @param   plugin
     *          Plugin
     */
    public FoliaPlatform(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadPlayers() {
        super.loadPlayers();

        // Folia never calls PlayerChangedWorldEvent, this is a workaround
        TAB.getInstance().getCpu().getProcessingThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), ()  -> {
            PerWorldPlayerList pwp = null;
            boolean pwpChecked = false;
            for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
                Player bukkitPlayer = (Player) player.getPlayer();
                String worldName = bukkitPlayer.getWorld().getName();
                if (player.world.getName().equals(worldName)) continue;
                TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), World.byName(worldName));
                if (!pwpChecked) {
                    pwp = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST);
                    pwpChecked = true;
                }
                if (pwp != null) {
                    PerWorldPlayerList finalPwp = pwp;
                    runSync(bukkitPlayer, () -> finalPwp.onWorldChange(new PlayerChangedWorldEvent(bukkitPlayer, bukkitPlayer.getWorld())));
                }
            }
        }, "Folia compatibility", "Refreshing world"), 100);
    }

    @Override
    public void registerPlaceholders() {
        super.registerPlaceholders();
        DecimalFormat decimal2;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        decimal2 = new DecimalFormat("#.##", symbols);
        registerInternalSyncPlaceholder(TabConstants.Placeholder.MSPT, p -> decimal2.format(Bukkit.getAverageTickTime()));
        registerInternalSyncPlaceholder(TabConstants.Placeholder.TPS, p -> decimal2.format(Math.min(20, Bukkit.getTPS()[0])));
    }

    @Override
    public void registerSyncPlaceholder(@NotNull String identifier) {
        String syncedPlaceholder = "%" + identifier.substring(6);
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
            runSync((Entity) p.getPlayer(), () -> {
                long time = System.nanoTime();
                String output = isPlaceholderAPI() ? PlaceholderAPI.setPlaceholders((Player) p.getPlayer(), syncedPlaceholder) : identifier;
                long totalTime =  System.nanoTime()-time;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
                TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, totalTime);
                TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output)); // To ensure player is loaded
            });
            return null;
        });
    }

    private void registerInternalSyncPlaceholder(@NonNull String identifier, @NonNull Function<TabPlayer, String> function) {
        PlayerPlaceholderImpl[] ppl = new PlayerPlaceholderImpl[1];
        ppl[0] = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(identifier, p -> {
            runSync((Entity) p.getPlayer(), () -> {
                long time = System.nanoTime();
                String output = function.apply((TabPlayer) p);
                long totalTime =  System.nanoTime()-time;
                TAB.getInstance().getCPUManager().addPlaceholderTime(identifier, totalTime);
                TAB.getInstance().getCpu().addTime(TAB.getInstance().getPlaceholderManager().getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, totalTime);
                TAB.getInstance().getCPUManager().runTask(() -> ppl[0].updateValue(p, output)); // To ensure player is loaded
            });
            return null;
        });
    }

    /**
     * Overriding the method to fix initial error caused by ServerPlaceholder implementation trying to
     * retrieve the value in constructor, but folia does not support that. Overriding this function to avoid the
     * MSPT function being called in the wrong thread.
     *
     * @return  -1
     */
    @Override
    public double getMSPT() {
        return -1;
    }

    /**
     * Overriding the method to fix initial error caused by ServerPlaceholder implementation trying to
     * retrieve the value in constructor, but folia does not support that. Overriding this function to avoid the
     * TPS function being called in the wrong thread.
     *
     * @return  -1
     */
    @Override
    public double getTPS() {
        return -1;
    }

    /**
     * Runs task using entity's scheduler, which executes it on the region thread owning the entity.
     * If the entity is removed before the task could run, it is silently dropped.
     *
     * @param   entity
     *          entity to run task for
     * @param   task
     *          Task to run
     */
    @Override
    public void runSync(@NotNull Entity entity, @NotNull Runnable task) {
        if (!getPlugin().isEnabled()) return; // Server shutdown, no one cares anymore, everyone is about to be kicked
        entity.getScheduler().run(getPlugin(), scheduledTask -> task.run(), null);
    }

    /**
     * Runs task in global tick thread.
     *
     * @param   task
     *          Task to run
     */
    @Override
    public void runSyncGlobal(@NotNull Runnable task) {
        if (!getPlugin().isEnabled()) return; // Server shutdown, no one cares anymore, everyone is about to be kicked
        globalScheduler.execute(getPlugin(), task);
    }

    @Override
    public boolean hasLineOfSight(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return true; // Cross-entity line-of-sight checks are not safe from Folia's global scheduler.
    }
}
