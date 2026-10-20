package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class HousingTypeTest {

    @Test
    public void isValidHousingType_supportedValuesIgnoringCase_returnsTrue() {
        assertTrue(HousingType.isValidHousingType("HDB"));
        assertTrue(HousingType.isValidHousingType("hdb"));
        assertTrue(HousingType.isValidHousingType("Condo"));
        assertTrue(HousingType.isValidHousingType("CONDO"));
        assertTrue(HousingType.isValidHousingType("EC"));
        assertTrue(HousingType.isValidHousingType("Landed"));
    }

    @Test
    public void isValidHousingType_unsupportedValue_returnsFalse() {
        assertFalse(HousingType.isValidHousingType("Castle"));
        assertFalse(HousingType.isValidHousingType(""));
        assertFalse(HousingType.isValidHousingType(null));
    }

    @Test
    public void fromString_unsupportedValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, HousingType.MESSAGE_CONSTRAINTS, ()
                -> HousingType.fromString("Castle"));
    }

    @Test
    public void toString_returnsDisplayValue() {
        assertEquals("HDB", HousingType.HDB.toString());
        assertEquals("Condo", HousingType.CONDO.toString());
        assertEquals("EC", HousingType.EC.toString());
        assertEquals("Landed", HousingType.LANDED.toString());
    }

    @Test
    public void getDisplayValue_returnsDisplayValue() {
        assertEquals("HDB", HousingType.HDB.getDisplayValue());
        assertEquals("Condo", HousingType.CONDO.getDisplayValue());
        assertEquals("EC", HousingType.EC.getDisplayValue());
        assertEquals("Landed", HousingType.LANDED.getDisplayValue());
    }

    @Test
    public void fromString_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, HousingType.MESSAGE_CONSTRAINTS, ()
                -> HousingType.fromString(null));
    }
}
