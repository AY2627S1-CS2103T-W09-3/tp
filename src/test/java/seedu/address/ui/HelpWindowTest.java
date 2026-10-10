package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelpWindowTest {

    private static final String EXPECTED_USERGUIDE_URL =
            "https://ay2627s1-cs2103t-w09-3.github.io/tp/UserGuide.html";

    @Test
    public void userGuideUrl_pointsToTeamGuide() {
        assertEquals(EXPECTED_USERGUIDE_URL, HelpWindow.USERGUIDE_URL);
    }

    @Test
    public void helpMessage_refersToTeamGuide() {
        assertEquals("Refer to the user guide: " + EXPECTED_USERGUIDE_URL, HelpWindow.HELP_MESSAGE);
    }
}
