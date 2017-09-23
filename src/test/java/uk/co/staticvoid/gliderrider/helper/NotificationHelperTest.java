package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.staticvoid.gliderrider.business.Bookkeeper;
import uk.co.staticvoid.gliderrider.business.RecordManager;
import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class NotificationHelperTest {

    private static final String COURSE = "TestCourse";
    private static final String PLAYER = "TestPlayer";
    private static final String CHECKPOINT = "TestCheckpoint";

    private Bookkeeper bookkeeper = mock(Bookkeeper.class);
    private RecordManager recordManager = mock(RecordManager.class);
    private BukkitHelper bukkitHelper = mock(BukkitHelper.class);

    private NotificationHelper underTest = new NotificationHelper(bookkeeper, recordManager, bukkitHelper);

    private Player player = mock(Player.class);
    private Attempt attempt = mock(Attempt.class);

    private Checkpoint checkpoint = mock(Checkpoint.class);

    private Long TIME = 99L;

    @Before
    public void setup(){
        when(player.getDisplayName()).thenReturn(PLAYER);
        when(checkpoint.getCourse()).thenReturn(COURSE);

        when(checkpoint.getName()).thenReturn(CHECKPOINT);
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.of(attempt));
        when(attempt.getCourse()).thenReturn(COURSE);
        when(attempt.getCourseTime(CHECKPOINT)).thenReturn(TIME);
    }

    @Test
    public void shouldSendConsoleMessageToPlayerWhenPassingCheckpoint() {
        when(checkpoint.getType()).thenReturn(CheckpointType.START);
        underTest.informPlayerOfCheckpoint(player, checkpoint);
        Mockito.verify(bukkitHelper).consoleNotification(player, "TestCheckpoint - 00:00.099");
    }

    @Test
    public void shouldNotSendMessageIfCheckpointNotInAttempt() {
        when(checkpoint.getType()).thenReturn(CheckpointType.START);
        when(bookkeeper.getAttempt(PLAYER, COURSE)).thenReturn(Optional.empty());

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        verifyZeroInteractions(bukkitHelper);
    }

    @Test
    public void shouldDisplayFailedTitleNotificationIfTheyFailedTheCourse() throws Exception {
        when(checkpoint.getType()).thenReturn(CheckpointType.START);
        when(attempt.isFailed()).thenReturn(true);

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        Mockito.verify(bukkitHelper).titleNotification(player, ChatColor.RED + NotificationHelper.ATTEMPT_FAILED, NotificationHelper.FAILED_REASON);
    }

    @Test
    public void shouldDisplayInTheLeadTitleNotificationIfTheyAreInTheLeadAtTheFinish() {
        when(checkpoint.getType()).thenReturn(CheckpointType.FINISH);
        when(attempt.isFailed()).thenReturn(false);
        when(recordManager.isTheFastestTime(attempt)).thenReturn(true);

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        Mockito.verify(bukkitHelper).titleNotification(player, ChatColor.GREEN + NotificationHelper.YOUR_IN_THE_LEAD, "00:00.099");
    }

    @Test
    public void shouldNotTellThePlayerTheyAreInTheLeadBeforeTheFinish() {
        when(checkpoint.getType()).thenReturn(CheckpointType.STAGE);
        when(attempt.isFailed()).thenReturn(false);
        when(recordManager.isTheFastestTime(attempt)).thenReturn(true);

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        Mockito.verify(bukkitHelper, times(0)).titleNotification(player, ChatColor.GREEN + NotificationHelper.YOUR_IN_THE_LEAD, "00:00.099");
    }

    @Test
    public void shouldNotTellThePlayerTheyAreInTheLeadIfThereAttemptFailed() {
        when(checkpoint.getType()).thenReturn(CheckpointType.FINISH);
        when(attempt.isFailed()).thenReturn(true);
        when(recordManager.isTheFastestTime(attempt)).thenReturn(true);

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        Mockito.verify(bukkitHelper, times(0)).titleNotification(player, ChatColor.GREEN + NotificationHelper.YOUR_IN_THE_LEAD, "00:00.099");
    }

    @Test
    public void shouldTellThePlayerWhenTheyCompleteTheCourse(){
        when(checkpoint.getType()).thenReturn(CheckpointType.FINISH);
        when(attempt.isFailed()).thenReturn(false);
        when(recordManager.isTheFastestTime(attempt)).thenReturn(false);

        underTest.informPlayerOfCheckpoint(player, checkpoint);

        Mockito.verify(bukkitHelper).titleNotification(player, ChatColor.GREEN + NotificationHelper.COURSE_COMPLETE, "00:00.099");
    }

}
