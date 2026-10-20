package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;

/**
 * Finds and lists all clients whose name, phone or email contains any of the argument keywords,
 * ignoring case.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds clients by name, phone or email.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]\n"
            + "Example: " + COMMAND_WORD + " John";

    private final PersonMatchesSearchTermsPredicate predicate;

    public FindCommand(PersonMatchesSearchTermsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);

        int count = model.getFilteredPersonList().size();
        String message;
        if (count == 0) {
            message = Messages.MESSAGE_NO_CLIENTS_FOUND;
        } else if (count == 1) {
            message = Messages.MESSAGE_ONE_CLIENT_LISTED;
        } else {
            message = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count);
        }

        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
