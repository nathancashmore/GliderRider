package uk.co.staticvoid.gliderrider.business;

import org.apache.commons.collections.ListUtils;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.exception.CheckpointWithSameNameException;
import uk.co.staticvoid.gliderrider.exception.CourseAlreadyCompleteException;
import uk.co.staticvoid.gliderrider.exception.StartAlreadyExistsException;
import uk.co.staticvoid.gliderrider.exception.StartMustStartException;
import uk.co.staticvoid.gliderrider.helper.ConfigHelper;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CheckpointManager {

    public static final String CONFIG_FILE = "checkpoint.yml";

    private GliderRider plugin;
    private ConfigHelper configHelper;
    private CheckpointArtist checkpointArtist;

    public CheckpointManager(GliderRider plugin, ConfigHelper configHelper, CheckpointArtist checkpointArtist) {
        this.plugin = plugin;
        this.configHelper = configHelper;
        this.checkpointArtist = checkpointArtist;
    }

    public Checkpoint createCheckpoint(String name, String course, CheckpointType type, Location location) {
        List<Checkpoint> checkpointList = getCheckpoints();

        validateCreateParameters(name, course, type, checkpointList);

        int checkpointRadius = plugin.getConfig().getInt("checkpoint-radius");

        Checkpoint newCheckpoint = new Checkpoint(name, course, type, location, checkpointRadius);
        checkpointList.add(newCheckpoint);

        configHelper.getConfig().set("checkpoint", checkpointList);
        configHelper.saveConfig();

        checkpointArtist.drawCheckpoint(newCheckpoint);

        return newCheckpoint;
    }

    private void validateCreateParameters(String name, String course, CheckpointType type, List<Checkpoint> checkpointList) {

        if(checkpointList.stream()
                .filter(cp1 -> cp1.getCourse().equals(course))
                .count() == 0 && type != CheckpointType.START) {
            throw new StartMustStartException("You must create a START checkpoint first");
        }

        if(checkpointList.stream()
                .filter(cp1 -> cp1.getCourse().equals(course))
                .filter(cp2 -> cp2.getType().equals(CheckpointType.START))
                .count() != 0 && type.equals(CheckpointType.START) ) {
            throw new StartAlreadyExistsException("You cannot create a START for the course " + course + " as one already exists");
        }

        if(checkpointList.stream()
                .filter(cp1 -> cp1.getCourse().equals(course))
                .filter(cp2 -> cp2.getType().equals(CheckpointType.FINISH))
                .count() != 0) {
            throw new CourseAlreadyCompleteException("The course " + course + " already has a FINISH so no more checkpoints can be created");
        }

        if(checkpointList.stream()
                .filter(cp1 -> cp1.getCourse().equals(course))
                .filter(cp2 -> cp2.getName().equals(name))
                .count() != 0) {
            throw new CheckpointWithSameNameException("A checkpoint on the course already has that name");
        }

    }

    @SuppressWarnings("unchecked")
    public List<Checkpoint> getCheckpoints() {

        List<Checkpoint> checkpoints = (List<Checkpoint>)configHelper.getConfig().get("checkpoint");

        if(checkpoints == null) {
            return new ArrayList<>();
        }

        return checkpoints;
    }

    public Optional<Checkpoint> isCheckpoint(Location location) {
        List<Checkpoint> checkpointList = getCheckpoints();

        return checkpointList.stream()
                .filter(cp -> LocationHelper.isNear(cp.getLocation(), location, cp.getRadius()))
                .findFirst();
    }

    public void removeCourse(String course) {

        @SuppressWarnings("unchecked")
        List<Checkpoint> checkpoints = (List<Checkpoint>)configHelper.getConfig().get("checkpoint");
        List<Checkpoint> toRemove = getCourse(course);

        @SuppressWarnings("unchecked")
        List<Checkpoint> revisedCheckpoints = ListUtils.subtract(checkpoints, toRemove);

        configHelper.getConfig().set("checkpoint", revisedCheckpoints);
        configHelper.saveConfig();

        toRemove.forEach(cp -> checkpointArtist.eraseCheckpoint(cp));
    }

    public List<Checkpoint> getCourse(String course) {
        @SuppressWarnings("unchecked")
        List<Checkpoint> checkpoints = (List<Checkpoint>)configHelper.getConfig().get("checkpoint");

        return  checkpoints.stream()
                .filter(cp -> cp.getCourse().equals(course))
                .collect(Collectors.toList());
    }

    public int getNoOfCheckpoints(String course) {
        return getCourse(course).size();
    };
}
