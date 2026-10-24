package de.oberamsystems.utils;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void testLocalDateTimeToString() {
        LocalDateTime ldt = LocalDateTime.of(2026, 8, 26, 17, 45);
        String result = Utils.LocalDateTimeToString(ldt);
        assertEquals("26.08.2026 17:45", result);
    }

    @Test
    void testOffsetDateTimeToString() {
        OffsetDateTime odt = OffsetDateTime.of(2026, 8, 26, 17, 45, 0, 0, ZoneOffset.ofHours(2));
        String result = Utils.OffsetDateTimeToString(odt);
        assertEquals("26.08.2026 17:45", result);
    }

    @Test
    void testDurationToString() {
        Duration dur = Duration.ofDays(1).plusHours(2).plusMinutes(30);
        String result = Utils.DurationToString(dur);
        assertEquals("1 day(s) 2 hour(s) and 30 minute(s)", result);
    }

    @Test
    void testToOffset() {
        OffsetDateTime odt = Utils.toOffset(2026, 8, 26, 17, 45, ZoneOffset.ofHours(2));
        assertEquals(2026, odt.getYear());
        assertEquals(8, odt.getMonthValue());
        assertEquals(26, odt.getDayOfMonth());
        assertEquals(17, odt.getHour());
        assertEquals(45, odt.getMinute());
        assertEquals(ZoneOffset.ofHours(2), odt.getOffset());

        OffsetDateTime odtWithSeconds = Utils.toOffset(2026, 8, 26, 17, 45, 30, ZoneOffset.ofHours(2));
        assertEquals(30, odtWithSeconds.getSecond());
    }
}
