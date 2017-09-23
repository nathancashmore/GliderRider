package uk.co.staticvoid.gliderrider;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;
import uk.co.staticvoid.gliderrider.helper.NotificationHelper;

import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class})
public class GliderRiderListenerTest {

    private static final String CHECKPOINT = "StartTestCheckpoint";
    private static final String COURSE = "TestCourse";
    private static final String PLAYER = "TestPlayer";
    private static final String WORLD = "TestWorld";
    private static final Integer RADIUS = 2;
    private static final Float NORTH_YAW = -180f;
    private static final Double X = 10d;
    private static final Double Y = 10d;
    private static final Double Z = 10d;

    private GliderRider plugin = PowerMockito.mock(GliderRider.class);
    private Logger logger = mock(Logger.class);
    private Player player = mock(Player.class);
    private org.bukkit.Location bukkitLocation = mock(org.bukkit.Location.class);
    private World world = mock(World.class);

    private PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, bukkitLocation, bukkitLocation);

    private CheckpointManager checkpointManager = mock(CheckpointManager.class);
    private Bookkeeper bookkeeper = mock(Bookkeeper.class);
    private NotificationHelper notificationHelper = mock(NotificationHelper.class);

    private GliderRiderListener underTest = new GliderRiderListener(checkpointManager, bookkeeper, notificationHelper);

    private Checkpoint checkpoint;
    private Location pluginLocation;

    @Before
    public void setup() {
        when(plugin.getLogger()).thenReturn(logger);

        when(world.getName()).thenReturn(WORLD);
        when(bukkitLocation.getWorld()).thenReturn(world);
        when(bukkitLocation.getX()).thenReturn(X);
        when(bukkitLocation.getY()).thenReturn(Y);
        when(bukkitLocation.getZ()).thenReturn(Z);
        when(bukkitLocation.getYaw()).thenReturn(NORTH_YAW);

        pluginLocation = new Location(WORLD, X.intValue(), Y.intValue(), Z.intValue(), Direction.N);
        checkpoint = new Checkpoint(CHECKPOINT, COURSE, CheckpointType.START, pluginLocation, RADIUS);

        when(player.getLocation()).thenReturn(bukkitLocation);
        when(player.getDisplayName()).thenReturn(PLAYER);
    }

    @Test
    public void shouldNotifyPlayerIfPassingCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(checkpoint));

        underTest.onMove(playerMoveEvent);

        Mockito.verify(bookkeeper).seen(PLAYER, checkpoint);
        Mockito.verify(notificationHelper).informPlayerOfCheckpoint(player, checkpoint);
    }

    @Test
    public void shouldNotNotifyPlayerIfNotPassingCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.empty());

        underTest.onMove(playerMoveEvent);

        Mockito.verifyZeroInteractions(bookkeeper);
        Mockito.verifyZeroInteractions(notificationHelper);
    }

    @Test
    public void shouldOnlySendMessageOnceForTheSameCheckpoint(){
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(checkpoint));

        underTest.onMove(playerMoveEvent);
        underTest.onMove(playerMoveEvent);
        underTest.onMove(playerMoveEvent);

        Mockito.verify(notificationHelper, times(1)).informPlayerOfCheckpoint(player, checkpoint);
    }

}