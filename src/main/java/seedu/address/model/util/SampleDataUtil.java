package seedu.address.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Area;
import seedu.address.model.person.Budget;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Email;
import seedu.address.model.person.HousingType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Price;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {

    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                    new Address("Blk 30 Geylang Street 29, #06-40"), ClientRole.BUYER,
                    new Note("Looking for a three-bedroom unit near an MRT station.\n"
                            + "Call after 6 PM on weekdays."),
                    getTagSet("firsttime"),
                    new Budget(new Price("800000"), new Price("1200000")), null,
                    getAreaSet("Bishan", "Toa Payoh"), getHousingTypeSet("HDB", "Condo")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                    new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"), ClientRole.BUYER,
                    null,
                    getTagSet("colleagues", "upgrading"), null, null,
                    getAreaSet(), getHousingTypeSet()),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                    new Address("Blk 11 Ang Mo Kio Street 74, #11-04"), ClientRole.SELLER,
                    null,
                    getTagSet("urgent"), null, new Price("950000"),
                    getAreaSet(), getHousingTypeSet()),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                    new Address("Blk 436 Serangoon Gardens Street 26, #16-43"), ClientRole.SELLER,
                    null,
                    getTagSet("weekend"), null, null,
                    getAreaSet(), getHousingTypeSet()),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                    new Address("Blk 47 Tampines Street 20, #17-35"), ClientRole.BOTH,
                    null,
                    getTagSet("upgrading"),
                    new Budget(new Price("700000"), new Price("900000")), new Price("850000"),
                    getAreaSet("Tampines"), getHousingTypeSet("HDB", "EC"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

    /**
     * Returns an area set containing the list of strings given.
     */
    public static Set<Area> getAreaSet(String... strings) {
        return Arrays.stream(strings)
                .map(Area::new)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a housing type set containing the list of strings given.
     */
    public static Set<HousingType> getHousingTypeSet(String... strings) {
        return Arrays.stream(strings)
                .map(HousingType::fromString)
                .collect(Collectors.toSet());
    }

}
