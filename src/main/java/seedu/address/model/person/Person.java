package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable,
 * except for the role, note, budget and asking price, which may be absent.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final ClientRole role;
    private final Note note;
    private final Set<Tag> tags = new HashSet<>();
    private final Budget budget;
    private final Price askingPrice;
    private final Set<Area> areas = new HashSet<>();
    private final Set<HousingType> housingTypes = new HashSet<>();

    /**
     * Every field must be present and not null, except {@code role}, {@code note},
     * {@code budget} and {@code askingPrice}, which are null when the client has not supplied them.
     */
    public Person(Name name, Phone phone, Email email, Address address, ClientRole role,
            Note note, Set<Tag> tags, Budget budget, Price askingPrice, Set<Area> areas,
            Set<HousingType> housingTypes) {
        requireAllNonNull(name, phone, email, address, tags, areas, housingTypes);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.role = role;
        this.note = note;
        this.tags.addAll(tags);
        this.budget = budget;
        this.askingPrice = askingPrice;
        this.areas.addAll(areas);
        this.housingTypes.addAll(housingTypes);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns the client's role, or null if no role has been assigned yet.
     */
    public ClientRole getRole() {
        return role;
    }

    /**
     * Returns the client's note, or null if no note has been added yet.
     */
    public Note getNote() {
        return note;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns the client's budget, or null if none has been supplied.
     */
    public Budget getBudget() {
        return budget;
    }

    /**
     * Returns the client's asking price, or null if none has been supplied.
     */
    public Price getAskingPrice() {
        return askingPrice;
    }

    /**
     * Returns an immutable set of areas, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Area> getAreas() {
        return Collections.unmodifiableSet(areas);
    }

    /**
     * Returns an immutable set of housing types, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<HousingType> getHousingTypes() {
        return Collections.unmodifiableSet(housingTypes);
    }

    /**
     * Returns true if both persons have the same name (ignoring case), phone or email.
     * If any of the 3 are identical, persons are duplicates of each other.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }
        return otherPerson != null
                && (otherPerson.getName().isSameName(this.name)
                        || otherPerson.getPhone().equals(this.phone)
                        || otherPerson.getEmail().equals(this.email));
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && Objects.equals(role, otherPerson.role)
                && Objects.equals(note, otherPerson.note)
                && tags.equals(otherPerson.tags)
                && Objects.equals(budget, otherPerson.budget)
                && Objects.equals(askingPrice, otherPerson.askingPrice)
                && areas.equals(otherPerson.areas)
                && housingTypes.equals(otherPerson.housingTypes);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, role, note, tags, budget, askingPrice, areas, housingTypes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("role", role)
                .add("note", note)
                .add("tags", tags)
                .add("budget", budget)
                .add("askingPrice", askingPrice)
                .add("areas", areas)
                .add("housingTypes", housingTypes)
                .toString();
    }

}
