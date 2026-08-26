package de.oberamsystems.slm;

import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationConverterTest {

    @Test
    void testConvertWithDays() {
        DurationConverter converter = new DurationConverter();
        Duration dur = Duration.ofDays(2).plusHours(5).plusMinutes(15).plusSeconds(30);
        String result = converter.convert(dur);
        assertEquals("2D 05:15:30", result);
    }

    @Test
    void testConvertWithoutDays() {
        DurationConverter converter = new DurationConverter();
        Duration dur = Duration.ofHours(3).plusMinutes(5).plusSeconds(9);
        String result = converter.convert(dur);
        assertEquals("03:05:09", result);
    }
}
