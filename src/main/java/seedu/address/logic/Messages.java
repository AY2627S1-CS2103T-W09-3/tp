package seedu.address.logic;

import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_DUPLICATE_NAME =
            "A client with this name already exists in the client book.";
    private static final String COBUYER_WORKAROUND_HINT =
            "For co-buyers, use one household record tagged " + PREFIX_TAG + "couple.";
    public static final String MESSAGE_DUPLICATE_PHONE =
            "This phone number already belongs to another client: %1$s. " + COBUYER_WORKAROUND_HINT;
    public static final String MESSAGE_DUPLICATE_EMAIL =
            "This email address already belongs to another client: %1$s. " + COBUYER_WORKAROUND_HINT;

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message indicating which field of {@code candidate} clashes with {@code existing}.
     * Fields are checked in the order name, phone, email.
     */
    public static String getErrorMessageForDuplicatePerson(Person candidate, Person existing) {
        assert candidate.isSamePerson(existing);

        if (candidate.getName().isSameName(existing.getName())) {
            return MESSAGE_DUPLICATE_NAME;
        }
        if (candidate.getPhone().equals(existing.getPhone())) {
            return String.format(MESSAGE_DUPLICATE_PHONE, existing.getName());
        }
        return String.format(MESSAGE_DUPLICATE_EMAIL, existing.getName());
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

}
