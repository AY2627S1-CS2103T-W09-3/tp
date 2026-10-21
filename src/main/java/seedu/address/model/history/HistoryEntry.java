package seedu.address.model.history;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * Describes one successful data-changing command and the address-book states immediately before and after it.
 */
public final class HistoryEntry {

    private final AddressBookSnapshot beforeSnapshot;
    private final AddressBookSnapshot afterSnapshot;
    private final String commandWord;
    private final String actionDescription;

    /**
     * Creates a history entry.
     *
     * @param beforeSnapshot State immediately before the command was executed.
     * @param afterSnapshot State immediately after the command was executed.
     * @param commandWord Word used to invoke the command.
     * @param actionDescription User-facing description of the action.
     */
    public HistoryEntry(AddressBookSnapshot beforeSnapshot, AddressBookSnapshot afterSnapshot,
            String commandWord, String actionDescription) {
        this.beforeSnapshot = requireNonNull(beforeSnapshot);
        this.afterSnapshot = requireNonNull(afterSnapshot);
        this.commandWord = requireNonBlank(commandWord, "Command word");
        this.actionDescription = requireNonBlank(actionDescription, "Action description");
    }

    /**
     * Creates a history entry by taking defensive snapshots of {@code before} and {@code after}.
     */
    public static HistoryEntry from(ReadOnlyAddressBook before, ReadOnlyAddressBook after,
            String commandWord, String actionDescription) {
        requireNonNull(before);
        requireNonNull(after);
        return new HistoryEntry(AddressBookSnapshot.copyOf(before), AddressBookSnapshot.copyOf(after),
                commandWord, actionDescription);
    }

    public AddressBookSnapshot getBeforeSnapshot() {
        return beforeSnapshot;
    }

    public AddressBookSnapshot getAfterSnapshot() {
        return afterSnapshot;
    }

    public String getCommandWord() {
        return commandWord;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    /**
     * Returns true if the command changed the address-book state.
     */
    public boolean representsChange() {
        return !beforeSnapshot.equals(afterSnapshot);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HistoryEntry otherEntry)) {
            return false;
        }

        return beforeSnapshot.equals(otherEntry.beforeSnapshot)
                && afterSnapshot.equals(otherEntry.afterSnapshot)
                && commandWord.equals(otherEntry.commandWord)
                && actionDescription.equals(otherEntry.actionDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beforeSnapshot, afterSnapshot, commandWord, actionDescription);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("beforeSnapshot", beforeSnapshot)
                .add("afterSnapshot", afterSnapshot)
                .add("commandWord", commandWord)
                .add("actionDescription", actionDescription)
                .toString();
    }

    private static String requireNonBlank(String value, String fieldName) {
        requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
