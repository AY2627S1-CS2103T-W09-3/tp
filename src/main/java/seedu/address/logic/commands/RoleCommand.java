package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

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
    public static final String MESSAGE_INVALID_CLIENT_INDEX =
            "The client index provided is invalid.";
    public static final String MESSAGE_SUCCESS =
            "Role updated: %1$s is now marked as %2$s";

    private final Index index;
    private final ClientRole role;

    /**
     * Creates a {@code RoleCommand} that assigns {@code role} to the client at {@code index}.
     */
    public RoleCommand(Index index, ClientRole role) {
        requireAllNonNull(index, role);
        this.index = index;
        this.role = role;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_CLIENT_INDEX);
        }

        Person clientToEdit = lastShownList.get(index.getZeroBased());
        Person editedClient = new Person(
                clientToEdit.getName(), clientToEdit.getPhone(), clientToEdit.getEmail(),
                clientToEdit.getAddress(), clientToEdit.getRemark(), role, clientToEdit.getTags());

        model.setPerson(clientToEdit, editedClient);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_SUCCESS,
                editedClient.getName(), role.getDisplayValue()));
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
