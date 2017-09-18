package uk.co.staticvoid.gliderrider.business;

import org.bukkit.Material;
import uk.co.staticvoid.gliderrider.GliderRider;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Location;

import java.util.ArrayList;
import java.util.List;

public class CheckpointArtist {
    
    public static final Material START_MATERIAL = Material.GREEN_GLAZED_TERRACOTTA;
    public static final Material STAGE_MATERIAL = Material.YELLOW_GLAZED_TERRACOTTA;
    public static final Material FINISH_MATERIAL = Material.RED_GLAZED_TERRACOTTA;
    public static final Material LIGHT_MATERIAL = Material.GLOWSTONE;

    enum Action {
        ADD, REMOVE
    }

    private GliderRider plugin;
    private final BlockEditor blockEditor;

    public CheckpointArtist(GliderRider plugin, BlockEditor blockEditor) {
        this.plugin = plugin;
        this.blockEditor = blockEditor;
    }

    public void drawCheckpoint(Checkpoint checkpoint) {
        editCheckpoint(checkpoint, Action.ADD);
    }

    public void eraseCheckpoint(Checkpoint checkpoint) {
        editCheckpoint(checkpoint, Action.REMOVE);
    }

    private void editCheckpoint(Checkpoint checkpoint, Action action) {

        List<Location> frameLocationList = new ArrayList<>();
        List<Location> cornerLocationList = new ArrayList<>();

        switch (checkpoint.getLocation().getDirection()) {
            case N:
            case NE:
            case S:
            case SW:
                frameLocationList.addAll(getLocationsNorthAndSouth(checkpoint.getLocation(), checkpoint.getRadius()));
                cornerLocationList.addAll(getCornerLocationsNorthAndSouth(checkpoint.getLocation(), checkpoint.getRadius()));
                break;
            case E:
            case W:
            case NW:
            case SE:
                frameLocationList.addAll(getLocationsEastAndWest(checkpoint.getLocation(), checkpoint.getRadius()));
                cornerLocationList.addAll(getCornerLocationsEastAndWest(checkpoint.getLocation(), checkpoint.getRadius()));

        }

        frameLocationList.forEach(loc -> {
            Material material = action == Action.ADD ? getMaterial(checkpoint.getType()) : Material.AIR;
            blockEditor.changeBlockMaterial(loc, material);
        });

        cornerLocationList.forEach(loc -> {
            blockEditor.changeBlockMaterial(loc, LIGHT_MATERIAL);
        });

    }

    private List<Location> getCornerLocationsNorthAndSouth(Location startLocation, Integer checkpointRadius) {
        List<Location> locationList = new ArrayList<>();

        locationList.add(startLocation.clone().addY(checkpointRadius).addX(checkpointRadius));
        locationList.add(startLocation.clone().addY(checkpointRadius).minusX(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius).minusX(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius).addX(checkpointRadius));

        return locationList;
    }

    private List<Location> getLocationsNorthAndSouth(Location startLocation, int checkpointRadius) {
        List<Location> locationList = new ArrayList<>();

        locationList.add(startLocation.clone().addY(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius));
        locationList.add(startLocation.clone().addX(checkpointRadius));
        locationList.add(startLocation.clone().minusX(checkpointRadius));

        for(int a=1; a < checkpointRadius; a++) {
            locationList.add(startLocation.clone().addY(checkpointRadius).minusX(a));
            locationList.add(startLocation.clone().addY(checkpointRadius).addX(a));

            locationList.add(startLocation.clone().addX(checkpointRadius).minusY(a));
            locationList.add(startLocation.clone().addX(checkpointRadius).addY(a));

            locationList.add(startLocation.clone().minusX(checkpointRadius).minusY(a));
            locationList.add(startLocation.clone().minusX(checkpointRadius).addY(a));

            locationList.add(startLocation.clone().minusY(checkpointRadius).minusX(a));
            locationList.add(startLocation.clone().minusY(checkpointRadius).addX(a));
        }

        return locationList;
    }

    private List<Location> getCornerLocationsEastAndWest(Location startLocation, Integer checkpointRadius) {
        List<Location> locationList = new ArrayList<>();

        locationList.add(startLocation.clone().addY(checkpointRadius).addZ(checkpointRadius));
        locationList.add(startLocation.clone().addY(checkpointRadius).minusZ(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius).minusZ(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius).addZ(checkpointRadius));

        return locationList;
    }

    private List<Location> getLocationsEastAndWest(Location startLocation, int checkpointRadius){
        List<Location> locationList = new ArrayList<>();

        locationList.add(startLocation.clone().addY(checkpointRadius));
        locationList.add(startLocation.clone().minusY(checkpointRadius));
        locationList.add(startLocation.clone().addZ(checkpointRadius));
        locationList.add(startLocation.clone().minusZ(checkpointRadius));

        for(int a=1; a < checkpointRadius; a++) {
            locationList.add(startLocation.clone().addY(checkpointRadius).minusZ(a));
            locationList.add(startLocation.clone().addY(checkpointRadius).addZ(a));

            locationList.add(startLocation.clone().minusY(checkpointRadius).minusZ(a));
            locationList.add(startLocation.clone().minusY(checkpointRadius).addZ(a));

            locationList.add(startLocation.clone().addZ(checkpointRadius).minusY(a));
            locationList.add(startLocation.clone().addZ(checkpointRadius).addY(a));

            locationList.add(startLocation.clone().minusZ(checkpointRadius).minusY(a));
            locationList.add(startLocation.clone().minusZ(checkpointRadius).addY(a));
        }

        return locationList;
    }

    private Material getMaterial(CheckpointType type) {
        switch (type) {
            case START:
                return START_MATERIAL;
            case STAGE:
                return STAGE_MATERIAL;
            case FINISH:
                return FINISH_MATERIAL;
            default:
                throw new IllegalArgumentException("Checkpoint material is not valid");
        }
    }


}
