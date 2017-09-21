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

            switch (cmd.getName().toLowerCase()) {
                case "checkpoint-create":
                    validateInputFromPlayer(sender);
                    validateCheckpointType(args[2]);
                    validateMustHaveThreeArguments(args);
                    createCheckpoint(args[0], args[1], args[2].toUpperCase(), (Player) sender);
                    return true;
                case "checkpoint-list":
                    listCheckpoints(sender);
                    return true;
                case "course-remove":
                    validateMustHaveOneArgument(args);
                    removeCourse(args[0], sender);
                    removeRecords(args[0], sender);
                    return true;
                case "checkpoint-radius":
                    validateInputFromPlayer(sender);
                    validateMustHaveOneArgument(args);
                    resizeCheckpoint(args[0], (Player) sender);
                    return true;
                case "course-records":
                    validateMustHaveOneArgument(args);
                    listCourseRecords(args[0], sender);
                    return true;
                    case "records-remove":
                    validateMustHaveOneArgument(args);
                    removeRecords(args[0], sender);
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

    private void removeRecords(String course, CommandSender commandSender) {
        recordManager.removeRecord(course);
        commandSender.sendMessage("Records for " + course + " removed");
    }

    private void listCourseRecords(String course, CommandSender commandSender) {
        List<CourseTime> courseTimeList = recordManager.getCourseTimes(course);

        commandSender.sendMessage("-- Leaderboard for " + course + " --\n");
        courseTimeList.forEach(tl -> {
            commandSender.sendMessage(tl.getPlayer() + " - " + formatter.format(tl.getTime()) + "\n");
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

    private void listCheckpoints(CommandSender commandSender) {
        List<Checkpoint> checkpointList = checkpointManager.getCheckpoints();

        if (checkpointList.isEmpty()) {
            commandSender.sendMessage("There are no checkpoints");
        } else {
            commandSender.sendMessage("--- Checkpoints ---");
            checkpointList.forEach(cp -> commandSender.sendMessage(cp.toString()));
        }
    }

    private void removeCourse(String course, CommandSender commandSender) {
        checkpointManager.removeCourse(course);
        commandSender.sendMessage("Course " + course + " removed");
        listCheckpoints(commandSender);
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
