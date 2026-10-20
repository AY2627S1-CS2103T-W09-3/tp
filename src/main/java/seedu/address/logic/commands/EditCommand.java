package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_AREA;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ASKING_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_BUDGET;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOUSING_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the client identified "
            + "by the index number used in the displayed client list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_BUDGET + "MIN-MAX] "
            + "[" + PREFIX_ASKING_PRICE + "PRICE] "
            + "[" + PREFIX_AREA + "AREA]... "
            + "[" + PREFIX_HOUSING_TYPE + "TYPE]... "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com "
            + PREFIX_BUDGET + "100000-200000 "
            + PREFIX_AREA + "Bukit Timah "
            + PREFIX_HOUSING_TYPE + "Condo";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited client: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        Optional<Person> existingPerson = model.findPersonOtherThan(personToEdit, editedPerson);
        if (existingPerson.isPresent()) {
            throw new CommandException(Messages.getErrorMessageForDuplicatePerson(editedPerson, existingPerson.get()));
        }

        model.setPerson(personToEdit, editedPerson);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(personToEdit);

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());
        Budget updatedBudget = editPersonDescriptor.isBudgetEdited()
                ? editPersonDescriptor.getBudget().orElse(null)
                : personToEdit.getBudget();
        Price updatedAskingPrice = editPersonDescriptor.isAskingPriceEdited()
                ? editPersonDescriptor.getAskingPrice().orElse(null)
                : personToEdit.getAskingPrice();
        Set<Area> updatedAreas = editPersonDescriptor.getAreas().orElse(personToEdit.getAreas());
        Set<HousingType> updatedHousingTypes =
                editPersonDescriptor.getHousingTypes().orElse(personToEdit.getHousingTypes());

        return new Person(updatedName, updatedPhone, updatedEmail, updatedAddress,
                personToEdit.getRole(), personToEdit.getNote(), updatedTags, updatedBudget,
                updatedAskingPrice, updatedAreas, updatedHousingTypes);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;
        private Budget budget;
        private boolean budgetEdited;
        private Price askingPrice;
        private boolean askingPriceEdited;
        private Set<Area> areas;
        private Set<HousingType> housingTypes;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags}, {@code areas} and {@code housingTypes} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
            setAreas(toCopy.areas);
            setHousingTypes(toCopy.housingTypes);
            this.budget = toCopy.budget;
            this.budgetEdited = toCopy.budgetEdited;
            this.askingPrice = toCopy.askingPrice;
            this.askingPriceEdited = toCopy.askingPriceEdited;
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, tags, areas, housingTypes)
                    || budgetEdited || askingPriceEdited;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Sets {@code budget} and marks the budget as edited. A null value clears the budget.
         */
        public void setBudget(Budget budget) {
            this.budget = budget;
            this.budgetEdited = true;
        }

        public Optional<Budget> getBudget() {
            return Optional.ofNullable(budget);
        }

        public boolean isBudgetEdited() {
            return budgetEdited;
        }

        /**
         * Sets {@code askingPrice} and marks the asking price as edited. A null value clears it.
         */
        public void setAskingPrice(Price askingPrice) {
            this.askingPrice = askingPrice;
            this.askingPriceEdited = true;
        }

        public Optional<Price> getAskingPrice() {
            return Optional.ofNullable(askingPrice);
        }

        public boolean isAskingPriceEdited() {
            return askingPriceEdited;
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        /**
         * Sets {@code areas} to this object's {@code areas}.
         * A defensive copy of {@code areas} is used internally.
         */
        public void setAreas(Set<Area> areas) {
            this.areas = (areas != null) ? new HashSet<>(areas) : null;
        }

        /**
         * Returns an unmodifiable area set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code areas} is null.
         */
        public Optional<Set<Area>> getAreas() {
            return (areas != null) ? Optional.of(Collections.unmodifiableSet(areas)) : Optional.empty();
        }

        /**
         * Sets {@code housingTypes} to this object's {@code housingTypes}.
         * A defensive copy of {@code housingTypes} is used internally.
         */
        public void setHousingTypes(Set<HousingType> housingTypes) {
            this.housingTypes = (housingTypes != null) ? new HashSet<>(housingTypes) : null;
        }

        /**
         * Returns an unmodifiable housing type set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code housingTypes} is null.
         */
        public Optional<Set<HousingType>> getHousingTypes() {
            return (housingTypes != null) ? Optional.of(Collections.unmodifiableSet(housingTypes)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && Objects.equals(budget, otherEditPersonDescriptor.budget)
                    && budgetEdited == otherEditPersonDescriptor.budgetEdited
                    && Objects.equals(askingPrice, otherEditPersonDescriptor.askingPrice)
                    && askingPriceEdited == otherEditPersonDescriptor.askingPriceEdited
                    && Objects.equals(areas, otherEditPersonDescriptor.areas)
                    && Objects.equals(housingTypes, otherEditPersonDescriptor.housingTypes);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("tags", tags)
                    .add("budget", budget)
                    .add("budgetEdited", budgetEdited)
                    .add("askingPrice", askingPrice)
                    .add("askingPriceEdited", askingPriceEdited)
                    .add("areas", areas)
                    .add("housingTypes", housingTypes)
                    .toString();
        }
    }
}
