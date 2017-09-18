package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.Bukkit;
import org.bukkit.World;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;

public class LocationHelper {

    public static Location toPluginLocation(org.bukkit.Location bukkitLocation) {
        return new Location(
                bukkitLocation.getWorld().getName(),
                (int)bukkitLocation.getX(),
                (int)bukkitLocation.getY(),
                (int)bukkitLocation.getZ(),
                getDirection(bukkitLocation)
        );
    }

    public static org.bukkit.Location toBukkitLocation(Location location) {
        World world = Bukkit.getServer().getWorld(location.getWorld());

        return new org.bukkit.Location(
                world,
                location.getX().doubleValue(),
                location.getY().doubleValue(),
                location.getZ().doubleValue());
    }

    private static Direction getDirection(org.bukkit.Location location) {
        double rotation = (location.getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return Direction.W;
        } else if (22.5 <= rotation && rotation < 67.5) {
            return Direction.NW;
        } else if (67.5 <= rotation && rotation < 112.5) {
            return Direction.N;
        } else if (112.5 <= rotation && rotation < 157.5) {
            return Direction.NE;
        } else if (157.5 <= rotation && rotation < 202.5) {
            return Direction.E;
        } else if (202.5 <= rotation && rotation < 247.5) {
            return Direction.SE;
        } else if (247.5 <= rotation && rotation < 292.5) {
            return Direction.S;
        } else if (292.5 <= rotation && rotation < 337.5) {
            return Direction.SW;
        } else if (337.5 <= rotation && rotation < 360.0) {
            return Direction.W;
        } else {
            return null;
        }
    }

    public static boolean isNear(Location a, Location b, int radius) {
        return a.getWorld().equals(b.getWorld()) &&
                (a.getX() >= b.getX() - radius) && (a.getX() <= b.getX() + radius) &&
                (a.getY() >= b.getY() - radius) && (a.getY() <= b.getY() + radius) &&
                (a.getZ() >= b.getZ() - radius) && (a.getZ() <= b.getZ() + radius);
    }
}