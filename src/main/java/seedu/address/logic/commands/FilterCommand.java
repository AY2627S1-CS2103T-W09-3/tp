package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonMatchesFilterPredicate;

/**
 * Filters the displayed clients by role and tag, replacing any filter or search that is active.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Filters clients by role and tag.\n"
            + "Parameters: [" + PREFIX_ROLE + "ROLE] [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_ROLE + "buyer " + PREFIX_TAG + "urgent";

    public static final String MESSAGE_SHOWING = "Showing %1$d of %2$d clients.";

    private final PersonMatchesFilterPredicate predicate;

    public FilterCommand(PersonMatchesFilterPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);

        // The total is the whole address book, not the filtered list.
        int shownCount = model.getFilteredPersonList().size();
        int totalCount = model.getAddressBook().getPersonList().size();
        return new CommandResult(String.format(MESSAGE_SHOWING, shownCount, totalCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FilterCommand otherFilterCommand)) {
            return false;
        }

        return predicate.equals(otherFilterCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
