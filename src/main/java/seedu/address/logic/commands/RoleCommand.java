package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;

/**
 * Changes the role of an existing client in the client book.
 */
public class RoleCommand extends Command {

    public static final String COMMAND_WORD = "role";

    public static final String MESSAGE_INVALID_COMMAND_FORMAT =
            "Invalid command format. Use: role INDEX ROLE.";
    public static final String MESSAGE_INVALID_ROLE =
            "Invalid Role: Role must be 'buyer', 'seller', 'both', or 'none'.";
    public static final String MESSAGE_INVALID_CLIENT_INDEX =
            "Invalid client index: Enter a number between 1 and %d for the displayed list.";
    public static final String MESSAGE_NO_DISPLAYED_CLIENTS =
            "Invalid client index: No clients are currently displayed.";
    public static final String MESSAGE_SUCCESS =
            "Role updated: %1$s is now marked as %2$s";
    public static final String MESSAGE_ROLE_CLEARED =
            "Role cleared: %1$s has no assigned role";
    public static final String MESSAGE_ROLE_ALREADY_UNASSIGNED =
            "Role unchanged: %1$s already has no assigned role";

    private String actionDescription = "";

    private final BigInteger oneBasedIndex;
    private final ClientRole role;

    /**
     * Creates a {@code RoleCommand} that assigns {@code role} to the client at {@code index}.
     * A null role clears an existing assignment.
     */
    public RoleCommand(Index index, ClientRole role) {
        this(BigInteger.valueOf(requireNonNull(index).getOneBased()), role);
    }

    /**
     * Creates a {@code RoleCommand} with a numeric index that may be outside the
     * displayed list, so it can report the valid range when executed.
     */
    public RoleCommand(BigInteger oneBasedIndex, ClientRole role) {
        this.oneBasedIndex = requireNonNull(oneBasedIndex);
        this.role = role;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        actionDescription = "";
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (lastShownList.isEmpty()) {
            throw new CommandException(MESSAGE_NO_DISPLAYED_CLIENTS);
        }
        if (oneBasedIndex.compareTo(BigInteger.ONE) < 0
                || oneBasedIndex.compareTo(BigInteger.valueOf(lastShownList.size())) > 0) {
            throw new CommandException(String.format(MESSAGE_INVALID_CLIENT_INDEX, lastShownList.size()));
        }

        Index index = Index.fromOneBased(oneBasedIndex.intValueExact());
        Person clientToEdit = lastShownList.get(index.getZeroBased());
        if (role == null && clientToEdit.getRole() == null) {
            return new CommandResult(String.format(MESSAGE_ROLE_ALREADY_UNASSIGNED, clientToEdit.getName()), index);
        }

        Person editedClient = new Person(
                clientToEdit.getName(), clientToEdit.getPhone(), clientToEdit.getEmail(),
                clientToEdit.getAddress(), role, clientToEdit.getNote(), clientToEdit.getTags(),
                clientToEdit.getBudget(), clientToEdit.getAskingPrice(), clientToEdit.getAreas(),
                clientToEdit.getHousingTypes());

        model.setPerson(clientToEdit, editedClient);
        actionDescription = role == null ? "Cleared role for " + editedClient.getName()
                : "Changed role for " + editedClient.getName() + " to " + role.getDisplayValue();

        String feedback = role == null ? String.format(MESSAGE_ROLE_CLEARED, editedClient.getName())
                : String.format(MESSAGE_SUCCESS, editedClient.getName(), role.getDisplayValue());
        int updatedIndex = model.getFilteredPersonList().indexOf(editedClient);
        return updatedIndex < 0 ? new CommandResult(feedback)
                : new CommandResult(feedback, Index.fromZeroBased(updatedIndex));
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public String getCommandWord() {
        return COMMAND_WORD;
    }

    @Override
    public String getActionDescription() {
        return actionDescription;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof RoleCommand otherRoleCommand
                && oneBasedIndex.equals(otherRoleCommand.oneBasedIndex)
                && role == otherRoleCommand.role;
    }
}
