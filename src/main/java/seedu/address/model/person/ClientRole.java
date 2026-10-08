package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents the role a client plays in a property transaction.
 * Guarantees: immutable; only the three declared values exist.
 */
public enum ClientRole {

    BUYER("buyer", "Buyer"),
    SELLER("seller", "Seller"),
    BOTH("both", "Buyer, Seller");

    /**
     * Message shown when the user supplies a role that is not buyer, seller or both.
     */
    public static final String MESSAGE_CONSTRAINTS =
            "Invalid Role: Role must be 'buyer', 'seller', or 'both'.";

    private final String value;
    private final String displayValue;

    /**
     * Constructs a {@code ClientRole}.
     *
     * @param value The token the user types and the data file stores.
     * @param displayValue The text shown to the user.
     */
    ClientRole(String value, String displayValue) {
        this.value = value;
        this.displayValue = displayValue;
    }

    /**
     * Returns the canonical token, which is what the user types and what the data file stores.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the text shown to the user.
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns true if {@code test} names one of the three roles, ignoring case.
     * Leading and trailing whitespace is not accepted; trimming is the parser's job.
     */
    public static boolean isValidRole(String test) {
        requireNonNull(test);
        for (ClientRole role : values()) {
            if (role.value.equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@code ClientRole} named by {@code role}, ignoring case.
     *
     * @throws IllegalArgumentException if {@code role} is not buyer, seller or both.
     */
    public static ClientRole fromString(String role) {
        requireNonNull(role);
        for (ClientRole candidate : values()) {
            if (candidate.value.equalsIgnoreCase(role)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return getDisplayValue();
    }
}
