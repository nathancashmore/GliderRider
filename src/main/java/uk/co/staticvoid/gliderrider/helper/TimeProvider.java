package uk.co.staticvoid.gliderrider.helper;

import org.joda.time.DateTime;

public class TimeProvider {

    public Long timeNow() {
        return DateTime.now().getMillis();
    }

}
