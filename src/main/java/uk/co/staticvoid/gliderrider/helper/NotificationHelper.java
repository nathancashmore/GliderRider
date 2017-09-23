package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.RecordManager;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class NotificationHelper {

    public static final String ATTEMPT_FAILED = "Attempt failed :-(";
    public static final String FAILED_REASON = "You missed a checkpoint";
    public static final String YOUR_IN_THE_LEAD = "Your in the lead !!";
    public static final String IS_IN_THE_LEAD = " is in the lead";
    public static final String COURSE_COMPLETE = "Course complete";

    private final Bookkeeper bookkeeper;
    private final RecordManager recordManager;
    private final BukkitHelper bukkitHelper;

    private DateFormat timeDisplay = new SimpleDateFormat("mm:ss.SSS");

    public NotificationHelper(Bookkeeper bookkeeper, RecordManager recordManager, BukkitHelper bukkitHelper) {
        this.bookkeeper = bookkeeper;
        this.recordManager = recordManager;
        this.bukkitHelper = bukkitHelper;
    }

    public void informPlayerOfCheckpoint(Player player, Checkpoint checkpoint) {
        Optional<Attempt> attempt = bookkeeper.getAttempt(player.getDisplayName(), checkpoint.getCourse());

        attempt.ifPresent(att -> {
            Long checkpointTime = att.getCourseTime(checkpoint.getName());
            String displayTime = timeDisplay.format(checkpointTime);
            String checkpointDisplayTime = checkpoint.getName() + " - " + displayTime;

            if (checkpointTime != null) {
                bukkitHelper.consoleNotification(player, checkpointDisplayTime);
            }

            if (att.isFailed()) {
                bukkitHelper.titleNotification(player, ChatColor.RED + ATTEMPT_FAILED, FAILED_REASON);
            }

            if (checkpoint.getType().equals(CheckpointType.FINISH) && !att.isFailed()) {
                if (recordManager.isTheFastestTime(att)) {
                    bukkitHelper.titleNotification(player, ChatColor.GREEN + YOUR_IN_THE_LEAD, displayTime);
                    bukkitHelper.broadcastMessage(ChatColor.YELLOW + player.getDisplayName() + IS_IN_THE_LEAD);
                } else {
                    bukkitHelper.titleNotification(player, ChatColor.GREEN + COURSE_COMPLETE, displayTime);
                }
            }
        });
    }


}
