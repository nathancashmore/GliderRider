package uk.co.staticvoid.gliderrider;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.co.staticvoid.gliderrider.business.CheckpointManager;
import uk.co.staticvoid.gliderrider.business.RecordManager;

import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GliderRider.class})
public class GliderRiderCommandExecutorTest {

    private GliderRider plugin = PowerMockito.mock(GliderRider.class);
    private Logger logger = mock(Logger.class);
    private CheckpointManager checkpointManager = mock(CheckpointManager.class);
    private RecordManager recordManager = mock(RecordManager.class);

    private Player player = mock(Player.class);
    private CaveSpider caveSpider = mock(CaveSpider.class);
    private org.bukkit.Location location = mock(org.bukkit.Location.class);
    private World world = mock(World.class);
    private FileConfiguration fileConfiguration = mock(FileConfiguration.class);

    private Command command = mock(Command.class);

    private GliderRiderCommandExecutor underTest = new GliderRiderCommandExecutor(plugin, checkpointManager, recordManager);

    @Before
    public void setup() {
        when(plugin.getLogger()).thenReturn(logger);

        when(plugin.getConfig()).thenReturn(fileConfiguration);

        when(location.getWorld()).thenReturn(world);
        when(location.getX()).thenReturn(1d);
        when(location.getY()).thenReturn(2d);
        when(location.getZ()).thenReturn(3d);

        when(player.getLocation()).thenReturn(location);
    }

    @Test
    public void shouldBeAbleToCreateACheckpoint() {
        when(command.getName()).thenReturn("checkpoint-create");
        String[] inputArgs = {"test-checkpoint", "test-course", "start"};

        assertThat(underTest.onCommand(player, command, "",  inputArgs), is(true));
    }

    @Test
    public void shouldBeAbleToRemoveACheckpoint() {
        when(command.getName()).thenReturn("course-remove");
        String[] inputArgs = {"test-checkpoint"};

        assertThat(underTest.onCommand(player, command, "",  inputArgs), is(true));
    }

    @Test
    public void shouldBeAbleToListCheckpoints() {
        when(command.getName()).thenReturn("checkpoint-list");

        String[] inputArgs = {};
        assertThat(underTest.onCommand(player, command, "", inputArgs ), is(true));
    }

    @Test
    public void shouldBeAbleToChangeCheckpointRadius() {
        when(command.getName()).thenReturn("checkpoint-radius");

        String[] inputArgs = { "10" };
        assertThat(underTest.onCommand(player, command, "", inputArgs ), is(true));
    }

    @Test
    public void shouldBeAbleToDisplayRecords() {
        when(command.getName()).thenReturn("course-records");

        String[] inputArgs = { "x" };
        assertThat(underTest.onCommand(player, command, "", inputArgs ), is(true));
    }

    @Test
    public void shouldBeAbleToRemoveRecords() {
        when(command.getName()).thenReturn("records-remove");

        String[] inputArgs = { "x" };
        assertThat(underTest.onCommand(player, command, "", inputArgs ), is(true));
    }

    @Test
    public void mustOnlyBeExecutedByAPlayer() {
        when(command.getName()).thenReturn("checkpoint-create");
        String[] inputArgs = {"test-course", "start"};

        assertThat(underTest.onCommand(caveSpider, command, "",  inputArgs), is(false));
    }

    @Test
    public void mustHaveThreeArgumentsForCreateCheckpoint() {
        when(command.getName()).thenReturn("checkpoint-create");
        String[] inputArgs = {"test-checkpoint", "test-course"};

        assertThat(underTest.onCommand(player, command, "", inputArgs), is(false));
    }

    @Test
    public void mustHaveValidCheckpointTypeForCreateCheckpoint() {
        when(command.getName()).thenReturn("checkpoint-create");
        String[] inputArgs = {"test-course", "CRAZY"};

        assertThat(underTest.onCommand(player, command, "", inputArgs), is(false));
    }

    @Test
    public void mustHaveOneArgumentForRemoveCourse() {
        when(command.getName()).thenReturn("checkpoint-remove-course");
        String[] inputArgs = {};

        assertThat(underTest.onCommand(player, command, "", inputArgs), is(false));
    }

    @Test
    public void mustHaveOneArgumentForCheckpointResize() {
        when(command.getName()).thenReturn("checkpoint-radius");
        String[] inputArgs = {};

        assertThat(underTest.onCommand(player, command, "", inputArgs), is(false));
    }

    @Test
    public void mustHaveANumberArgumentForCheckpointResize() {
        when(command.getName()).thenReturn("checkpoint-radius");
        String[] inputArgs = {"NotANumber"};

        assertThat(underTest.onCommand(player, command, "", inputArgs), is(false));
    }

}