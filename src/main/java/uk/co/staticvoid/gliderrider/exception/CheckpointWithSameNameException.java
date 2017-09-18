package uk.co.staticvoid.gliderrider.exception;

public class CheckpointWithSameNameException extends IllegalArgumentException {
    public CheckpointWithSameNameException(String s) {
        super(s);
    }
}
