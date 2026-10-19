package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book after the user has confirmed.
 * Without confirmation, warns the user and leaves the address book unchanged.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRM_WORD = "confirm";

    public static final String MESSAGE_SUCCESS = "Client book has been cleared!";
    public static final String MESSAGE_EMPTY = "The client book is already empty.";
    public static final String MESSAGE_WARNING_ONE_CLIENT = "This will delete the only client. Type "
            + COMMAND_WORD + " " + CONFIRM_WORD + " to proceed.";
    public static final String MESSAGE_WARNING_MULTIPLE_CLIENTS = "This will delete all %1$d clients. Type "
            + COMMAND_WORD + " " + CONFIRM_WORD + " to proceed.";

    private final boolean isConfirmed;

    /**
     * Creates a {@code ClearCommand} that clears the address book only if {@code isConfirmed} is true.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);

        // Counts the whole address book, not the filtered list, because clear also deletes hidden clients.
        int clientCount = model.getAddressBook().getPersonList().size();

        if (clientCount == 0) {
            return new CommandResult(MESSAGE_EMPTY);
        }

        if (!isConfirmed) {
            String warning = clientCount == 1
                    ? MESSAGE_WARNING_ONE_CLIENT
                    : String.format(MESSAGE_WARNING_MULTIPLE_CLIENTS, clientCount);
            return new CommandResult(warning, false, false, true);
        }

        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ClearCommand otherClearCommand)) {
            return false;
        }

        return isConfirmed == otherClearCommand.isConfirmed;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isConfirmed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
