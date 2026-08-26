package de.oberamsystems.utils;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void testLocalDateTimeToString() {
        LocalDateTime ldt = LocalDateTime.of(2026, 8, 26, 17, 45);
        String result = Utils.LocalDateTimeToString(ldt);
        assertEquals("26.08.2026 17:45", result);
    }

    @Test
    void testDurationToString() {
        Duration dur = Duration.ofDays(1).plusHours(2).plusMinutes(30);
        String result = Utils.DurationToString(dur);
        assertEquals("1 day(s) 2 hour(s) and 30 minute(s)", result);
    }
}
