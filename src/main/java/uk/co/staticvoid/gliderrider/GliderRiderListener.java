package uk.co.staticvoid.gliderrider;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;
import uk.co.staticvoid.gliderrider.helper.NotificationHelper;

import java.util.HashMap;
import java.util.Map;

public final class GliderRiderListener implements Listener {

    private final CheckpointManager checkpointManager;
    private final Bookkeeper bookkeeper;
    private final NotificationHelper notificationHelper;

    private Map<String, String> lastSeen = new HashMap<>();

    public GliderRiderListener(CheckpointManager checkpointManager, Bookkeeper bookkeeper, NotificationHelper notificationHelper) {
        this.checkpointManager = checkpointManager;
        this.bookkeeper = bookkeeper;
        this.notificationHelper = notificationHelper;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        checkpointManager.isCheckpoint(
            LocationHelper.toPluginLocation(player.getLocation())).ifPresent(
            cp -> {
                bookkeeper.seen(player.getDisplayName(), cp);

                if (!cp.getName().equals(lastSeen.get(player.getDisplayName()))) {
                    notificationHelper.informPlayerOfCheckpoint(player, cp);
                    lastSeen.put(player.getDisplayName(), cp.getName());
                }
            });
    }
}
