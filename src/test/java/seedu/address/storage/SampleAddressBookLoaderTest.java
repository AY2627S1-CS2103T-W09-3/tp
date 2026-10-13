package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ClientRole;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;

/**
 * Tests bundled sample loading and fallback behavior.
 */
public class SampleAddressBookLoaderTest {

    @Test
    public void load_bundledResource_returnsPropertyClients() throws Exception {
        try (InputStream input = SampleAddressBookLoader.class
                .getResourceAsStream("/data/sample-addressbook.json")) {
            assertNotNull(input);
        }

        List<Person> persons = SampleAddressBookLoader.load().getPersonList();

        assertEquals(5, persons.size());
        assertEquals("Alicia Tan", persons.get(0).getName().fullName);
        assertEquals(2L, persons.stream().filter(p -> p.getRole() == ClientRole.BUYER).count());
        assertEquals(2L, persons.stream().filter(p -> p.getRole() == ClientRole.SELLER).count());
        assertEquals(1L, persons.stream().filter(p -> p.getRole() == ClientRole.BOTH).count());
        assertTrue(persons.stream().allMatch(p -> p.getRole() != null));
        assertTrue(persons.stream().allMatch(p -> !p.getTags().isEmpty()));
    }

    @Test
    public void load_missingResource_returnsFallback() {
        assertEquals(SampleDataUtil.getSampleAddressBook(), SampleAddressBookLoader.load(null));
    }

    @Test
    public void load_malformedJson_returnsFallback() {
        assertFallback("{invalid json");
    }

    @Test
    public void load_invalidStructure_returnsFallback() {
        assertFallback("null");
        assertFallback("{}");
        assertFallback("{\"persons\": [null]}");
    }

    @Test
    public void load_invalidClient_returnsFallback() {
        assertFallback("""
                {
                  "persons": [{
                    "name": "Invalid Client",
                    "phone": "not-a-phone",
                    "email": "invalid@example.com",
                    "address": "Singapore",
                    "role": "buyer",
                    "tags": []
                  }]
                }
                """);
    }

    @Test
    public void load_unreadableStream_returnsFallbackAndClosesStream() {
        FailingInputStream input = new FailingInputStream();

        assertEquals(SampleDataUtil.getSampleAddressBook(), SampleAddressBookLoader.load(input));
        assertTrue(input.isClosed);
    }

    @Test
    public void getSampleAddressBook_fallbackSamples_havePropertyRoles() {
        List<Person> persons = SampleDataUtil.getSampleAddressBook().getPersonList();

        assertTrue(persons.size() >= 5 && persons.size() <= 6);
        assertTrue(persons.stream().allMatch(p -> p.getRole() != null));
        assertTrue(persons.stream().filter(p -> p.getRole() == ClientRole.BUYER).count() >= 2);
        assertTrue(persons.stream().filter(p -> p.getRole() == ClientRole.SELLER).count() >= 2);
        assertTrue(persons.stream().anyMatch(p -> p.getRole() == ClientRole.BOTH));
    }

    /**
     * Verifies that invalid bundled JSON uses the samples defined in code.
     */
    private void assertFallback(String json) {
        InputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        ReadOnlyAddressBook result = SampleAddressBookLoader.load(input);

        assertEquals(SampleDataUtil.getSampleAddressBook(), result);
    }

    /**
     * Simulates an unreadable resource and records whether it is closed.
     */
    private static class FailingInputStream extends InputStream {
        private boolean isClosed;

        @Override
        public int read() throws IOException {
            throw new IOException("Cannot read sample resource.");
        }

        @Override
        public void close() {
            isClosed = true;
        }
    }
}
