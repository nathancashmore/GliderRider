package uk.co.staticvoid.gliderrider.task;

import org.bukkit.scheduler.BukkitRunnable;
import uk.co.staticvoid.gliderrider.business.RecordManager;

import java.io.IOException;

public class RecordExportRunnable extends BukkitRunnable {

    private final RecordManager recordManager;

    public RecordExportRunnable(RecordManager recordManager) {
        this.recordManager = recordManager;
    }

    @Override
    public void run() {
        try {
            recordManager.outputAsJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
