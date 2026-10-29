package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOUSING_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.PersonMatchesFilterPredicate;

/**
 * Filters the displayed clients by role, tag, area, housing type, budget and asking price, replacing any filter or
 * search that is active.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Filters clients by role, tag, area, housing type, budget and asking price.\n"
            + "Parameters: [" + PREFIX_ROLE + "ROLE] [" + PREFIX_TAG + "TAG]... [" + PREFIX_AREA + "AREA]... ["
            + PREFIX_HOUSING_TYPE + "TYPE]... [" + PREFIX_BUDGET + "BUDGET] [" + PREFIX_ASKING_PRICE + "PRICE]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_ROLE + "buyer " + PREFIX_AREA + "Bedok "
            + PREFIX_BUDGET + "500000-600000";

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
        return new CommandResult(Messages.getShowingMessage(shownCount, totalCount));
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
