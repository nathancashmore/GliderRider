package uk.co.staticvoid.gliderrider.business;

import uk.co.staticvoid.gliderrider.domain.Attempt;
import uk.co.staticvoid.gliderrider.domain.Checkpoint;
import uk.co.staticvoid.gliderrider.domain.CheckpointType;
import uk.co.staticvoid.gliderrider.exception.PlayerCheatedException;
import uk.co.staticvoid.gliderrider.helper.TimeProvider;

import java.util.*;
import java.util.stream.Collectors;

public class Bookkeeper {

    private final TimeProvider timeProvider;
    private final RecordManager recordManager;
    private final CheckpointManager checkpointManager;

    private List<Attempt> attemptList = new ArrayList<>();

    public Bookkeeper(TimeProvider timeProvider, RecordManager recordManager, CheckpointManager checkpointManager) {
        this.timeProvider = timeProvider;
        this.recordManager = recordManager;
        this.checkpointManager = checkpointManager;
    }

    public void seen(String player, Checkpoint checkpoint) throws PlayerCheatedException {
        Optional<Attempt> existingAttempt = getAttempt(player, checkpoint.getCourse());

        if(existingAttempt.isPresent() && !existingAttempt.get().isFinished()) {
            Attempt currentAttempt = existingAttempt.get();

            addCheckpointTime(currentAttempt, checkpoint);

            if(checkpoint.getType().equals(CheckpointType.FINISH)) {
                finaliseAttempt(currentAttempt);
            }
        }
        else if(checkpoint.getType().equals(CheckpointType.START)) {
            removePreviousAttempts(player, checkpoint.getCourse());
            addNewAttempt(player, checkpoint);
        }
    }

    private void removePreviousAttempts(String player, String course) {
        List<Attempt> oldAttempts = attemptList.stream()
                .filter(
                        attempt -> attempt.getPlayer().equals(player) &&
                                attempt.getCourse().equals(course) &&
                                attempt.isFinished())
                .collect(Collectors.toList());

        attemptList.removeAll(oldAttempts);
    }

    private void finaliseAttempt(Attempt attempt) throws PlayerCheatedException {
        attempt.setFinished(true);
        if(checkpointManager.getCourse(attempt.getCourse()).size() == attempt.getTimeRecord().size()) {
            recordManager.addRecord(attempt);
        } else {
            throw new PlayerCheatedException("Failed the attempt as you missed checkpoints");
        }
    }

    public Optional<Attempt> getAttempt(String player, String course) {
        return attemptList.stream()
                .filter(att -> att.getPlayer().equals(player) && att.getCourse().equals(course))
                .findFirst();
    }

    private void addNewAttempt(String player, Checkpoint checkpoint) {
        Map<String, Long> checkpointTime = new LinkedHashMap<>();
        checkpointTime.put(checkpoint.getName(), timeProvider.timeNow());

        attemptList.add(new Attempt(player,checkpoint.getCourse(), checkpointTime ));
    }

    private void addCheckpointTime(Attempt attempt, Checkpoint checkpoint) {
        Map<String, Long> timeRecord = attempt.getTimeRecord();
        timeRecord.put(checkpoint.getName(), timeProvider.timeNow());
    }

}
