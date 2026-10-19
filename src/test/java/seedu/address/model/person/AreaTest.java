package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AreaTest {

    @Test
    public void isValidArea_supportedAreasIgnoringCase_returnsTrue() {
        assertTrue(Area.isValidArea("Bishan"));
        assertTrue(Area.isValidArea("bishan"));
        assertTrue(Area.isValidArea("  BISHAN  "));
        assertTrue(Area.isValidArea("Ang Mo Kio"));
        assertTrue(Area.isValidArea("Toa Payoh"));
    }

    @Test
    public void isValidArea_unsupportedArea_returnsFalse() {
        assertFalse(Area.isValidArea("Atlantis"));
        assertFalse(Area.isValidArea(""));
        assertFalse(Area.isValidArea(" "));
        assertFalse(Area.isValidArea(null));
    }

    @Test
    public void constructor_unsupportedArea_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Area.MESSAGE_CONSTRAINTS, () -> new Area("Atlantis"));
    }

    @Test
    public void constructor_lowerCaseArea_storesCanonicalTitleCase() {
        assertEquals("Bishan", new Area("bishan").toString());
        assertEquals("Ang Mo Kio", new Area("ANG MO KIO").toString());
    }

    @Test
    public void getValue_lowerCaseArea_returnsCanonicalValue() {
        assertEquals("Bishan", new Area("bishan").getValue());
    }

    @Test
    public void equals() {
        Area area = new Area("Bishan");

        assertTrue(area.equals(new Area("bishan")));
        assertTrue(area.equals(area));
        assertFalse(area.equals(new Area("Bedok")));
        assertFalse(area.equals(null));
        assertEquals(area.hashCode(), new Area("BISHAN").hashCode());
    }
}
