package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class LocationHelperTest {

    private static final String WORLD_NAME = "TestWorld";

    private Server server = mock(Server.class);
    private World world = mock(World.class);
    private Logger logger = mock(Logger.class);

    private Location pluginLocation;
    private org.bukkit.Location bukkitLocation;

    @Before
    public void setup(){
        when(world.getName()).thenReturn(WORLD_NAME);
        when(server.getWorld(WORLD_NAME)).thenReturn(world);
        when(server.getLogger()).thenReturn(logger);

        mockStatic(Bukkit.class);
        when(Bukkit.getServer()).thenReturn(server);

        this.bukkitLocation = new org.bukkit.Location(world, 1,2,3, 10f, 0f);
        this.pluginLocation = new Location(WORLD_NAME, 1, 2, 3, Direction.N);
    }

    @Test
    public void toBukkitLocation() throws Exception {
        org.bukkit.Location result = LocationHelper.toBukkitLocation(pluginLocation);

        assertThat(result.getWorld().getName(), is(WORLD_NAME));
        assertThat(result.getX(), is(1d));
        assertThat(result.getY(), is(2d));
        assertThat(result.getZ(), is(3d));
    }

    @Test
    public void toPluginLocation() throws Exception {
        Location result = LocationHelper.toPluginLocation(bukkitLocation);

        assertThat(result.getWorld(), is(WORLD_NAME));
        assertThat(result.getX(), is(1));
        assertThat(result.getY(), is(2));
        assertThat(result.getZ(), is(3));
    }

    @Test
    public void directionBasedOnYawCalc() {
        Map<Float, Direction> yawDirection = new HashMap<>();

        yawDirection.put(-180f, Direction.N);
        yawDirection.put(-135f, Direction.NE);
        yawDirection.put(-90f, Direction.E);
        yawDirection.put(-45f, Direction.SE);
        yawDirection.put(0f, Direction.S);
        yawDirection.put(45f, Direction.SW);
        yawDirection.put(90f, Direction.W);
        yawDirection.put(135f, Direction.NW);

        yawDirection.forEach((yaw, direction) -> {
            org.bukkit.Location location = new org.bukkit.Location(world, 10, 10, 10, yaw, 0f);
            Location result = LocationHelper.toPluginLocation(location);
            assertThat(result.getDirection(), is(direction));
        });
    }

    @Test
    public void isNear() {
        int checkpointRadius = 2;
        Direction anyOldDirection = Direction.N;

        Location A = new Location(WORLD_NAME, 10, 10, 10, Direction.N);

        List<Location> locationsNear = Arrays.asList(
                new Location(WORLD_NAME, 8, 8, 8, anyOldDirection),
                new Location(WORLD_NAME, 9, 9, 9, anyOldDirection),
                new Location(WORLD_NAME, 10, 10, 10, anyOldDirection),
                new Location(WORLD_NAME, 11, 11, 11, anyOldDirection),
                new Location(WORLD_NAME, 12, 12, 12, anyOldDirection)
        );

        List<Location> locationsNotSoNear = Arrays.asList(
                new Location(WORLD_NAME, 13, 13, 13, Direction.N),
                new Location(WORLD_NAME, 7, 7, 7, Direction.N)
        );

        locationsNear.forEach(B -> assertThat(LocationHelper.isNear(A, B, checkpointRadius), is(true)));
        locationsNotSoNear.forEach(B -> assertThat(LocationHelper.isNear(A, B, checkpointRadius), is(false)));
    }

}