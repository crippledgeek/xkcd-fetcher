package se.disabledsecurity.xkcd.fetcher;

import org.junit.jupiter.api.Test;
import se.disabledsecurity.xkcd.fetcher.external.model.Xkcd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify that the system can handle comics with different dates.
 */
class ComicDateTest {

    @Test
    void testComicsWithDifferentDates() {
        // Create two comics with different dates
        Xkcd comic1 = new Xkcd(
            "8",                                           // month
            "",                                            // link
            "2025",                                        // year
            "",                                            // news
            "First Comic",                                 // safe_title
            "",                                            // transcript
            "This is the first comic",                     // alt
            "First Comic",                                 // title
            "1",                                           // day (August 1, 2025)
            2000,                                          // num
            "https://imgs.xkcd.com/comics/first_comic.png"  // img
        );

        Xkcd comic2 = new Xkcd(
            "7",                                           // month
            "",                                            // link
            "2025",                                        // year
            "",                                            // news
            "Second Comic",                                // safe_title
            "",                                            // transcript
            "This is the second comic",                    // alt
            "Second Comic",                                // title
            "31",                                          // day (July 31, 2025)
            1999,                                          // num
            "https://imgs.xkcd.com/comics/second_comic.png"  // img
        );

        // Verify the comics have different dates
        LocalDate date1 = parseDate(comic1);
        LocalDate date2 = parseDate(comic2);

        assertNotEquals(date1, date2, "Comics should have different dates");
        assertEquals(LocalDate.of(2025, 8, 1), date1, "First comic should be from August 1, 2025");
        assertEquals(LocalDate.of(2025, 7, 31), date2, "Second comic should be from July 31, 2025");
    }

    /**
     * Parse the date from an Xkcd comic.
     * 
     * @param comic The Xkcd comic
     * @return The parsed LocalDate
     */
    private LocalDate parseDate(Xkcd comic) {
        int year = Integer.parseInt(comic.year());
        int month = Integer.parseInt(comic.month());
        int day = Integer.parseInt(comic.day());
        return LocalDate.of(year, month, day);
    }
}