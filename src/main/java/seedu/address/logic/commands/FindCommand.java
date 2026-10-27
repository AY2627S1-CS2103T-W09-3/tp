package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.PersonMatchesSearchTermsPredicate;

/**
 * Finds and lists all clients where every one of the argument keywords is contained in their name, phone or email,
 * ignoring case.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds clients whose name, phone or email contains every keyword.\n"
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

        // The total is the whole address book, not the filtered list.
        int shownCount = model.getFilteredPersonList().size();
        int totalCount = model.getAddressBook().getPersonList().size();
        return new CommandResult(Messages.getShowingMessage(shownCount, totalCount));
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
