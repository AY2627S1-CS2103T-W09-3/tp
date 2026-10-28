package seedu.address.logic.parser;

/**
 * A prefix that marks the beginning of an argument in an arguments string.
 * E.g. 't/' in 'add James t/ friend'.
 */
public class Prefix {
    private final String prefix;
    private final boolean numericValue;

    public Prefix(String prefix) {
        this(prefix, false);
    }

    /**
     * Creates a prefix whose value can only start with a digit or a comma, such as a price or a budget.
     * Used to tell a real prefix apart from the same letters appearing inside free text, e.g. {@code s/o}.
     */
    public Prefix(String prefix, boolean numericValue) {
        this.prefix = prefix;
        this.numericValue = numericValue;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns true if this prefix introduces a purely numeric value.
     */
    public boolean hasNumericValue() {
        return numericValue;
    }

    @Override
    public String toString() {
        return getPrefix();
    }

    @Override
    public int hashCode() {
        return prefix == null ? 0 : prefix.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Prefix otherPrefix)) {
            return false;
        }

        return prefix.equals(otherPrefix.prefix);
    }
}
