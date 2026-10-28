package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
            "The client index provided is invalid.";
    public static final String MESSAGE_SUCCESS =
            "Role updated: %1$s is now marked as %2$s";
    public static final String MESSAGE_ROLE_CLEARED =
            "Role cleared: %1$s has no assigned role";
    public static final String MESSAGE_ROLE_ALREADY_UNASSIGNED =
            "Role unchanged: %1$s already has no assigned role";

    private String actionDescription = "";

    private final Index index;
    private final ClientRole role;

    /**
     * Creates a {@code RoleCommand} that assigns {@code role} to the client at {@code index}.
     * A null role clears an existing assignment.
     */
    public RoleCommand(Index index, ClientRole role) {
        this.index = requireNonNull(index);
        this.role = role;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        actionDescription = "";
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();

        if (index.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(MESSAGE_INVALID_CLIENT_INDEX);
        }

        Person clientToEdit = displayedClients.get(index.getZeroBased());
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
                && index.equals(otherRoleCommand.index)
                && role == otherRoleCommand.role;
    }
}
