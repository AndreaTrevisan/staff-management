package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonMapperTest {

    @Test
    void mapMapsAllFieldsIncludingUsername() {
        User user = User.builder().username("mario.rossi").build();
        Person person = Person.builder()
                .documentId("ID1")
                .name("Mario")
                .surname("Rossi")
                .birthDate(LocalDate.of(1990, 5, 15))
                .email("mario@example.com")
                .address("Via Roma")
                .phone("123")
                .user(user)
                .build();

        assertEquals("mario.rossi", PersonMapper.map(person).getUsername());
        assertEquals("Mario", PersonMapper.map(person).getName());
        assertEquals("Rossi", PersonMapper.map(person).getSurname());
        assertEquals("ID1", PersonMapper.map(person).getDocumentId());
    }

    @Test
    void mapSetsUsernameNullWhenUserIsNull() {
        Person person = Person.builder()
                .documentId("ID2")
                .name("Giulia")
                .surname("Verdi")
                .build();

        assertNull(PersonMapper.map(person).getUsername());
    }
}
