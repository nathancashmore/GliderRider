package uk.co.staticvoid.gliderrider.task;

import org.bukkit.scheduler.BukkitRunnable;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.helper.BukkitHelper;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;
import uk.co.staticvoid.gliderrider.helper.NotificationHelper;

import java.util.HashMap;
import java.util.Map;

public class MovementMonitorRunnable extends BukkitRunnable {

    private final CheckpointManager checkpointManager;
    private final Bookkeeper bookkeeper;
    private final NotificationHelper notificationHelper;
    private final BukkitHelper bukkitHelper;

    private Map<String, String> lastSeen = new HashMap<>();

    public MovementMonitorRunnable(CheckpointManager checkpointManager, Bookkeeper bookkeeper, NotificationHelper notificationHelper, BukkitHelper bukkitHelper) {
        this.checkpointManager = checkpointManager;
        this.bookkeeper = bookkeeper;
        this.notificationHelper = notificationHelper;
        this.bukkitHelper = bukkitHelper;
    }

    @Override
    public void run() {
        bukkitHelper.getOnlinePlayers().forEach(player ->
            checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(player.getLocation()))
                .ifPresent(
                    cp -> {
                        bookkeeper.seen(player.getDisplayName(), cp);

                        if (!cp.getName().equals(lastSeen.get(player.getDisplayName()))) {
                            notificationHelper.informPlayerOfCheckpoint(player, cp);
                            lastSeen.put(player.getDisplayName(), cp.getName());
                        }
                    })
        );
    }
}
