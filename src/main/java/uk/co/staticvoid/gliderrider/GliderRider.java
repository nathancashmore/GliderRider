package uk.co.staticvoid.gliderrider;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.staticvoid.gliderrider.business.*;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CourseRecord;
import uk.co.staticvoid.gliderrider.domain.CourseTime;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.helper.BukkitHelper;
import uk.co.staticvoid.gliderrider.helper.ConfigHelper;
import uk.co.staticvoid.gliderrider.helper.NotificationHelper;
import uk.co.staticvoid.gliderrider.helper.TimeProvider;
import uk.co.staticvoid.gliderrider.task.MovementMonitorRunnable;
import uk.co.staticvoid.gliderrider.task.RecordExportRunnable;

public final class GliderRider extends JavaPlugin {

    private static final Long EXPORT_TO_JSON_INTERVAL = 100L;
    private static final Long MOVEMENT_MONITOR_INTERVAL = 20L; //Approx one second

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        ConfigHelper checkpointConfigHelper = new ConfigHelper(this, CheckpointManager.CONFIG_FILE);
        ConfigHelper recordConfigHelper = new ConfigHelper(this, RecordManager.CONFIG_FILE);

        checkpointConfigHelper.saveDefaultConfig();
        recordConfigHelper.saveDefaultConfig();

        ConfigurationSerialization.registerClass(Location.class);
        ConfigurationSerialization.registerClass(Checkpoint.class);
        ConfigurationSerialization.registerClass(CourseRecord.class);
        ConfigurationSerialization.registerClass(CourseTime.class);

        BlockEditor blockEditor = new BlockEditor();
        TimeProvider timeProvider = new TimeProvider();
        BukkitHelper bukkitHelper = new BukkitHelper();
        RecordManager recordManager = new RecordManager(recordConfigHelper);

        CheckpointArtist checkpointArtist = new CheckpointArtist(this, blockEditor);
        CheckpointManager checkpointManager = new CheckpointManager(this, checkpointConfigHelper , checkpointArtist);
        Bookkeeper bookkeeper = new Bookkeeper(timeProvider, recordManager, checkpointManager);
        NotificationHelper notificationHelper = new NotificationHelper(bookkeeper, recordManager, bukkitHelper);

        RecordExportRunnable recordExportRunnable = new RecordExportRunnable(recordManager);
        MovementMonitorRunnable movementMonitorRunnable = new MovementMonitorRunnable(checkpointManager, bookkeeper, notificationHelper, bukkitHelper);

        CommandExecutor cmdExecutor = new GliderRiderCommandExecutor(this, checkpointManager, recordManager);

        this.getCommand("checkpoint-create").setExecutor(cmdExecutor);
        this.getCommand("checkpoint-list").setExecutor(cmdExecutor);
        this.getCommand("course-remove").setExecutor(cmdExecutor);
        this.getCommand("checkpoint-radius").setExecutor(cmdExecutor);
        this.getCommand("course-records").setExecutor(cmdExecutor);
        this.getCommand("records-remove").setExecutor(cmdExecutor);

        recordExportRunnable.runTaskTimer(this, 0L, EXPORT_TO_JSON_INTERVAL);
        movementMonitorRunnable.runTaskTimerAsynchronously(this, 0L, MOVEMENT_MONITOR_INTERVAL);
    }

    @Override
    public void onDisable() {
        getLogger().info("Has been disabled");
    }
    
}
