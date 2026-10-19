package seedu.address.model.person;

/**
 * Represents a type of housing that a client is interested in.
 */
public enum HousingType {

    HDB("HDB"),
    CONDO("Condo"),
    EC("EC"),
    LANDED("Landed");

    public static final String MESSAGE_CONSTRAINTS = "Housing type must be HDB, Condo, EC, or Landed.";

    private final String displayValue;

    HousingType(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the text shown to the user.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns true if a given string names one of the four housing types, ignoring case.
     */
    public static boolean isValidHousingType(String test) {
        if (test == null) {
            return false;
        }
        for (HousingType housingType : values()) {
            if (housingType.displayValue.equalsIgnoreCase(test)
                    || housingType.name().equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@code HousingType} named by {@code type}, ignoring case.
     *
     * @throws IllegalArgumentException if {@code type} is not a supported housing type.
     */
    public static HousingType fromString(String type) {
        if (type != null) {
            for (HousingType housingType : values()) {
                if (housingType.displayValue.equalsIgnoreCase(type)
                        || housingType.name().equalsIgnoreCase(type)) {
                    return housingType;
                }
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
