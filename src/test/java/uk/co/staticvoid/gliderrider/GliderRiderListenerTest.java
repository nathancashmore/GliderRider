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
import uk.co.staticvoid.gliderrider.business.RecordManager;
import uk.co.staticvoid.gliderrider.domain.*;
import uk.co.staticvoid.gliderrider.exception.PlayerCheatedException;
import uk.co.staticvoid.gliderrider.helper.LocationHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class, PlayerMoveEvent.class})
public class GliderRiderListenerTest {

    private static final String START_CHECKPOINT_NAME = "StartTestCheckpoint";
    private static final String STAGE_CHECKPOINT_NAME = "StageTestCheckpoint";
    private static final String FINISH_CHECKPOINT_NAME = "FinishTestCheckpoint";
    private static final String COURSE = "TestCourse";
    private static final Integer RADIUS = 2;
    private static final String WORLD = "TestWorld";
    private static final Float NORTH_YAW = -180f;
    private static final Double X = 10d;
    private static final Double Y = 10d;
    private static final Double Z = 10d;
    private static final String PLAYER = "TestPlayer";

    private static final Long ONE_MINUTE = 60000L;
    private static final Long ONE_SECOND = 1000L;
    private static final String PLAYER_CHEATED_MESSAGE = "Player Cheated Message";

    private GliderRider plugin = PowerMockito.mock(GliderRider.class);
    private Logger logger = mock(Logger.class);
    private Player player = mock(Player.class);
    private org.bukkit.Location bukkitLocation = mock(org.bukkit.Location.class);
    private World world = mock(World.class);
    private PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(player, bukkitLocation, bukkitLocation);

    private CheckpointManager checkpointManager = mock(CheckpointManager.class);
    private Bookkeeper bookkeeper = mock(Bookkeeper.class);
    private RecordManager recordManager = mock(RecordManager.class);

    private GliderRiderListener underTest = new GliderRiderListener(plugin, checkpointManager, bookkeeper, recordManager);

    private Map<String, Long> timeMap = new LinkedHashMap<>();
    private Attempt attempt = new Attempt(PLAYER, COURSE, timeMap);

    private Checkpoint START_CHECKPOINT;
    private Checkpoint STAGE_CHECKPOINT;
    private Checkpoint FINISH_CHECKPOINT;

    @Before
    public void setup() {
        when(plugin.getLogger()).thenReturn(logger);

        when(world.getName()).thenReturn(WORLD);
        when(bukkitLocation.getWorld()).thenReturn(world);
        when(bukkitLocation.getX()).thenReturn(X);
        when(bukkitLocation.getY()).thenReturn(Y);
        when(bukkitLocation.getZ()).thenReturn(Z);
        when(bukkitLocation.getYaw()).thenReturn(NORTH_YAW);

        Location pluginLocation = new Location(world.getName(), X.intValue(), Y.intValue(), Z.intValue(), Direction.N);
        START_CHECKPOINT = new Checkpoint(START_CHECKPOINT_NAME, COURSE, CheckpointType.START, pluginLocation, RADIUS);
        STAGE_CHECKPOINT = new Checkpoint(STAGE_CHECKPOINT_NAME, COURSE, CheckpointType.STAGE, pluginLocation, RADIUS);
        FINISH_CHECKPOINT = new Checkpoint(FINISH_CHECKPOINT_NAME, COURSE, CheckpointType.FINISH, pluginLocation, RADIUS);

        when(player.getLocation()).thenReturn(bukkitLocation);
        when(player.getDisplayName()).thenReturn(PLAYER);
    }

    @Test
    public void shouldSendCheckpointMessageToPlayerWhenPassingSTARTCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(START_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));

        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);

        underTest.onMove(playerMoveEvent);

        Mockito.verify(player).sendMessage("StartTestCheckpoint - 00:00:000");
    }

    @Test
    public void shouldSendCheckpointMessageToPlayerWhenPassingSTAGECheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(STAGE_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));

        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);
        timeMap.put(STAGE_CHECKPOINT_NAME, ONE_SECOND * 10);

        underTest.onMove(playerMoveEvent);

        Mockito.verify(player).sendMessage("StageTestCheckpoint - 00:09:000");
    }

    @Test
    public void shouldSendCheckpointMessageToPlayerWhenPassingFINISHCheckpoint() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(FINISH_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));

        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);
        timeMap.put(STAGE_CHECKPOINT_NAME, ONE_SECOND * 10);
        timeMap.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE + ONE_SECOND);

        underTest.onMove(playerMoveEvent);

        Mockito.verify(player).sendMessage("FinishTestCheckpoint - 01:00:000");
    }

    @Test
    public void shouldNotSendMessageIfCheckpointNotInAttempt() {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(FINISH_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.empty());

        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);
        timeMap.put(STAGE_CHECKPOINT_NAME, ONE_SECOND * 10);
        timeMap.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE + ONE_SECOND);

        underTest.onMove(playerMoveEvent);

        Mockito.verify(player, times(0)).sendMessage(Mockito.anyString());
    }

    @Test
    public void shouldOnlySendMessageOnceForTheSameCheckpoint(){
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(START_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));

        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);

        underTest.onMove(playerMoveEvent);
        underTest.onMove(playerMoveEvent);

        Mockito.verify(player, times(1)).sendMessage("StartTestCheckpoint - 00:00:000");
    }

    @Test
    public void shouldTellThePlayerIfTheyMissCheckpoints() throws Exception {
        when(checkpointManager.isCheckpoint(LocationHelper.toPluginLocation(bukkitLocation))).thenReturn(Optional.of(FINISH_CHECKPOINT));
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));

        Mockito.doThrow(new PlayerCheatedException(PLAYER_CHEATED_MESSAGE)).when(bookkeeper).seen(PLAYER, FINISH_CHECKPOINT);
        
        timeMap.put(START_CHECKPOINT_NAME, ONE_SECOND);
        timeMap.put(FINISH_CHECKPOINT_NAME, ONE_MINUTE + ONE_SECOND);

        underTest.onMove(playerMoveEvent);

        Mockito.verify(player, times(1)).sendMessage(PLAYER_CHEATED_MESSAGE);
    }

}