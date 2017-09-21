package uk.co.staticvoid.gliderrider;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.business.RecordManager;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GliderRiderListener implements Listener {

    private DateFormat timeDisplay = new SimpleDateFormat("mm:ss:SSS");

    private final GliderRider plugin;
    private final CheckpointManager checkpointManager;
    private final Bookkeeper bookkeeper;
    private final RecordManager recordManager;

    private Map<String, String> lastSeen = new HashMap<>();

    public GliderRiderListener(GliderRider plugin, CheckpointManager checkpointManager, Bookkeeper bookkeeper, RecordManager recordManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
        this.bookkeeper = bookkeeper;
        this.recordManager = recordManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        checkpointManager.isCheckpoint(
                LocationHelper.toPluginLocation(player.getLocation())).ifPresent(
                cp -> {
                    bookkeeper.seen(player.getDisplayName(), cp);

                    if (!cp.getName().equals(lastSeen.get(player.getDisplayName()))) {
                        informPlayerOfCheckpoint(player, cp);
                    }
                });
    }

    private void informPlayerOfCheckpoint(Player player, Checkpoint cp) {

        Optional<Attempt> attempt = bookkeeper.getAttempt(player.getDisplayName(), cp.getCourse());

        attempt.ifPresent(att -> {
            Long checkpointTime = att.getCourseTime(cp.getName());

            if (checkpointTime != null) {
                player.sendMessage(cp.getName() + " - " + timeDisplay.format(checkpointTime));
            }

            if(att.isFailed()) {
                player.sendMessage("Attempt failed");
            }

            if(recordManager.isTheFastestTime(att) &&
                    cp.getType().equals(CheckpointType.FINISH) &&
                        !att.isFailed())
            {
                player.sendMessage("Your in the lead");
            }

            lastSeen.put(player.getDisplayName(), cp.getName());
        });
    }

}
