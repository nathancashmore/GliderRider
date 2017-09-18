package uk.co.staticvoid.gliderrider;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.business.RecordManager;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.CourseTime;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

public class GliderRiderCommandExecutor implements CommandExecutor {

    private DateFormat formatter = new SimpleDateFormat("mm:ss:SSS");

    private final JavaPlugin plugin;
    private final CheckpointManager checkpointManager;
    private final RecordManager recordManager;

    public GliderRiderCommandExecutor(JavaPlugin plugin, CheckpointManager checkpointManager, RecordManager recordManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
        this.recordManager = recordManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            validateInputFromPlayer(sender);

            switch (cmd.getName().toLowerCase()) {
                case "checkpoint-create":
                    validateCheckpointType(args[2]);
                    validateMustHaveThreeArguments(args);
                    createCheckpoint(args[0], args[1], args[2].toUpperCase(), (Player) sender);
                    return true;
                case "checkpoint-list":
                    listCheckpoints((Player) sender);
                    return true;
                case "course-remove":
                    validateMustHaveOneArgument(args);
                    removeCourse(args[0], (Player) sender);
                    removeRecords(args[0], (Player) sender);
                    return true;
                case "checkpoint-radius":
                    validateMustHaveOneArgument(args);
                    resizeCheckpoint(args[0], (Player) sender);
                    return true;
                case "course-records":
                    validateMustHaveOneArgument(args);
                    listCourseRecords(args[0], (Player) sender);
                    return true;
                case "records-remove":
                    validateMustHaveOneArgument(args);
                    removeRecords(args[0], (Player) sender);
                    return true;
                default:
                    return false;
            }
        } catch (Exception ex) {
            sender.sendMessage(ex.getMessage());
            plugin.getLogger().log(Level.INFO, ex.getMessage());
            return false;
        }
    }

    private void removeRecords(String course, Player player) {
        recordManager.removeRecord(course);
        player.sendMessage("Records for " + course + " removed");
    }

    private void listCourseRecords(String course, Player player) {
        List<CourseTime> courseTimeList = recordManager.getCourseTimes(course);

        player.sendMessage("-- Leaderboard for " + course + " --\n");
        courseTimeList.forEach(tl -> {
            player.sendMessage(tl.getPlayer() + " - " + formatter.format(tl.getTime()) + "\n");
        });
    }

    private void resizeCheckpoint(String size, Player player) {
        try {
            Integer newSize = new Integer(size);

            plugin.getConfig().set("checkpoint-radius", newSize);
            plugin.saveConfig();
            plugin.reloadConfig();

            player.sendMessage("Checkpoint radius set to " + newSize.toString());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Parameter to this command must be a number");
        }
    }

    private void listCheckpoints(Player player) {
        List<Checkpoint> checkpointList = checkpointManager.getCheckpoints();

        if (checkpointList.isEmpty()) {
            player.sendMessage("There are no checkpoints");
        } else {
            player.sendMessage("--- Checkpoints ---");
            checkpointList.forEach(cp -> player.sendMessage(cp.toString()));
        }
    }

    private void removeCourse(String course, Player player) {
        checkpointManager.removeCourse(course);
        player.sendMessage("Course " + course + " removed");
        listCheckpoints(player);
    }

    private void createCheckpoint(String name, String course, String type, Player player) {
        checkpointManager.createCheckpoint(
                name,
                course,
                CheckpointType.valueOf(type),
                LocationHelper.toPluginLocation(player.getLocation()));

        player.sendMessage("Checkpoint created");
        listCheckpoints(player);
    }

    private void validateInputFromPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            throw new IllegalArgumentException("Must be a player to execute a command");
        }
    }

    private void validateCheckpointType(String arg) {

        try {
            CheckpointType.valueOf(arg.toUpperCase());
        } catch (IllegalArgumentException iax) {
            throw new IllegalArgumentException("Third parameter must be 'start' or 'stage' or 'finish'");
        }
    }

    private void validateMustHaveThreeArguments(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("You need more parameters for that command");
        }
    }

    private void validateMustHaveOneArgument(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You need more parameters for that command");
        }

    }

}
