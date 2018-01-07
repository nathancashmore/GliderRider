package uk.co.staticvoid.gliderrider.task;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.domain.Direction;
import uk.co.staticvoid.gliderrider.domain.Location;
import uk.co.staticvoid.gliderrider.helper.BukkitHelper;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;
import uk.co.staticvoid.gliderrider.helper.NotificationHelper;

import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class MovementMonitorRunnableTest {
    private static final String CHECKPOINT = "StartTestCheckpoint";
    private static final String COURSE = "TestCourse";
    private static final String PLAYER = "TestPlayer";
    private static final String WORLD = "TestWorld";
    private static final Integer RADIUS = 2;
    private static final Double X = 10d;
    private static final Double Y = 10d;
    private static final Double Z = 10d;

    private Logger logger = mock(Logger.class);
    private Player player = mock(Player.class);
    private org.bukkit.Location bukkitLocation = mock(org.bukkit.Location.class);
    private World world = mock(World.class);

    private CheckpointManager checkpointManager = mock(CheckpointManager.class);
    private Bookkeeper bookkeeper = mock(Bookkeeper.class);
    private NotificationHelper notificationHelper = mock(NotificationHelper.class);
    private BukkitHelper bukkitHelper = mock(BukkitHelper.class);

    private MovementMonitorRunnable underTest;

    private Checkpoint checkpoint;

    @Before
    public void setup() {
        when(bukkitHelper.getOnlinePlayers()).thenReturn(Collections.singletonList(player));
        when(bukkitHelper.getLogger()).thenReturn(logger);

        when(world.getName()).thenReturn(WORLD);
        when(bukkitLocation.getWorld()).thenReturn(world);

        Location pluginLocation = new Location(WORLD, X.intValue(), Y.intValue(), Z.intValue(), Direction.N);
        checkpoint = new Checkpoint(CHECKPOINT, COURSE, CheckpointType.START, pluginLocation, RADIUS);

        when(player.getLocation()).thenReturn(bukkitLocation);
        when(player.getDisplayName()).thenReturn(PLAYER);

        underTest = new MovementMonitorRunnable(checkpointManager, bookkeeper, notificationHelper, bukkitHelper);
    }

    @Test
    public void shouldNotifyPlayerIfPassingCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(checkpoint));

        underTest.run();

        Mockito.verify(bookkeeper).seen(PLAYER, checkpoint);
        Mockito.verify(notificationHelper).informPlayerOfCheckpoint(player, checkpoint);
    }

    @Test
    public void shouldNotNotifyPlayerIfNotPassingCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.empty());

        underTest.run();

        Mockito.verifyZeroInteractions(bookkeeper);
        Mockito.verifyZeroInteractions(notificationHelper);
    }

    @Test
    public void shouldOnlySendMessageOnceForTheSameCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(checkpoint));

        underTest.run();
        underTest.run();
        underTest.run();

        Mockito.verify(notificationHelper, times(1)).informPlayerOfCheckpoint(player, checkpoint);
    }

}