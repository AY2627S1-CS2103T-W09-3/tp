package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;

/**
 * Represents a Singapore planning area that a client is interested in.
 * Guarantees: immutable; is valid as declared in {@link #isValidArea(String)}.
 */
public class Area {

    public static final String MESSAGE_CONSTRAINTS = "Area must be one of the supported areas.";

    private static final List<String> VALID_AREAS = List.of(
            "Ang Mo Kio", "Bedok", "Bishan", "Bukit Batok", "Bukit Merah", "Bukit Panjang",
            "Bukit Timah", "Choa Chu Kang", "Clementi", "Downtown Core", "Geylang", "Hougang",
            "Jurong East", "Jurong West", "Kallang", "Marine Parade", "Newton", "Novena",
            "Orchard", "Pasir Ris", "Punggol", "Queenstown", "River Valley", "Rochor",
            "Sembawang", "Sengkang", "Serangoon", "Tampines", "Tanglin", "Toa Payoh",
            "Woodlands", "Yishun");

    private final String value;

    /**
     * Constructs an {@code Area}.
     *
     * @param area A valid area name.
     */
    public Area(String area) {
        requireNonNull(area);
        String normalisedArea = normaliseWhitespace(area);
        checkArgument(isValidArea(normalisedArea), MESSAGE_CONSTRAINTS);
        value = canonicalArea(normalisedArea);
    }

    /**
     * Returns true if a given string names one of the supported areas, ignoring case and repeated whitespace.
     */
    public static boolean isValidArea(String test) {
        if (test == null) {
            return false;
        }
        String normalisedTest = normaliseWhitespace(test);
        for (String area : VALID_AREAS) {
            if (area.equalsIgnoreCase(normalisedTest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the area in its canonical title-case form.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the canonical title-case spelling of {@code area}.
     *
     * @param area An area name already normalised by {@link #normaliseWhitespace(String)}.
     */
    private static String canonicalArea(String area) {
        for (String validArea : VALID_AREAS) {
            if (validArea.equalsIgnoreCase(area)) {
                return validArea;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    /**
     * Returns {@code text} trimmed, with every run of whitespace collapsed to a single space.
     */
    private static String normaliseWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Area otherArea)) {
            return false;
        }

        return value.equals(otherArea.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
