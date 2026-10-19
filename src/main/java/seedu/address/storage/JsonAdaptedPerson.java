package seedu.address.storage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;

    /**
     * Absent when the client has no role, in which case the key is left out of the data file.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String role;

    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Absent when the client has no budget, in which case the key is left out of the data file.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JsonAdaptedBudget budget;

    /**
     * Absent when the client has no asking price, in which case the key is left out of the data file.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final BigDecimal askingPrice;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> areas = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> housingTypes = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("role") String role,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("budget") JsonAdaptedBudget budget,
            @JsonProperty("askingPrice") BigDecimal askingPrice,
            @JsonProperty("areas") List<String> areas,
            @JsonProperty("housingTypes") List<String> housingTypes) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.role = role;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.budget = budget;
        this.askingPrice = askingPrice;
        if (areas != null) {
            this.areas.addAll(areas);
        }
        if (housingTypes != null) {
            this.housingTypes.addAll(housingTypes);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        role = source.getRole() == null ? null : source.getRole().getValue();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        budget = source.getBudget() == null ? null : new JsonAdaptedBudget(source.getBudget());
        askingPrice = source.getAskingPrice() == null ? null
                : BigDecimal.valueOf(source.getAskingPrice().getValue());
        areas.addAll(source.getAreas().stream()
                .map(Area::toString)
                .collect(Collectors.toList()));
        housingTypes.addAll(source.getHousingTypes().stream()
                .map(HousingType::toString)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        // Client books saved before roles were introduced do not have this field,
        // and a client is allowed to have no role.
        final ClientRole modelRole;
        if (role == null) {
            modelRole = null;
        } else if (!ClientRole.isValidRole(role)) {
            throw new IllegalValueException(ClientRole.MESSAGE_CONSTRAINTS);
        } else {
            modelRole = ClientRole.fromString(role);
        }

        final Budget modelBudget;
        if (budget == null) {
            modelBudget = null;
        } else {
            if (budget.min == null || budget.max == null) {
                throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                        Budget.class.getSimpleName()));
            }
            try {
                modelBudget = new Budget(toPrice(budget.min), toPrice(budget.max));
            } catch (IllegalArgumentException e) {
                throw new IllegalValueException(e.getMessage());
            }
        }

        final Price modelAskingPrice;
        if (askingPrice == null) {
            modelAskingPrice = null;
        } else {
            modelAskingPrice = toPrice(askingPrice);
        }

        final Set<Area> modelAreas = new HashSet<>();
        for (String area : areas) {
            if (!Area.isValidArea(area)) {
                throw new IllegalValueException(Area.MESSAGE_CONSTRAINTS);
            }
            modelAreas.add(new Area(area));
        }

        final Set<HousingType> modelHousingTypes = new HashSet<>();
        for (String housingType : housingTypes) {
            if (!HousingType.isValidHousingType(housingType)) {
                throw new IllegalValueException(HousingType.MESSAGE_CONSTRAINTS);
            }
            modelHousingTypes.add(HousingType.fromString(housingType));
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRole, modelTags,
                modelBudget, modelAskingPrice, modelAreas, modelHousingTypes);
    }

    /**
     * Converts a JSON price to a {@code Price}, rejecting fractional values.
     *
     * @throws IllegalValueException if the value is null, fractional or outside the price range.
     */
    private static Price toPrice(BigDecimal value) throws IllegalValueException {
        if (value == null || value.stripTrailingZeros().scale() > 0) {
            throw new IllegalValueException(Price.MESSAGE_CONSTRAINTS);
        }
        try {
            return new Price(value.toBigIntegerExact().toString());
        } catch (ArithmeticException | IllegalArgumentException e) {
            throw new IllegalValueException(Price.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Jackson-friendly version of a {@link Budget}.
     */
    static final class JsonAdaptedBudget {

        private final BigDecimal min;
        private final BigDecimal max;

        @JsonCreator
        JsonAdaptedBudget(@JsonProperty("min") BigDecimal min, @JsonProperty("max") BigDecimal max) {
            this.min = min;
            this.max = max;
        }

        JsonAdaptedBudget(Budget source) {
            min = BigDecimal.valueOf(source.getMin().getValue());
            max = BigDecimal.valueOf(source.getMax().getValue());
        }
    }

}
