package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    @Test
    void mapIncludesPersonDocumentIdWhenAvailable() {
        Person person = Person.builder().documentId("DOC42").build();
        User user = User.builder()
                .username("mario")
                .password("pwd")
                .enabled(true)
                .roles(Collections.singleton("STAFF"))
                .person(person)
                .build();

        assertEquals("DOC42", UserMapper.map(user).getPersonDocumentId());
        assertTrue(UserMapper.map(user).isEnabled());
    }

    @Test
    void mapLeavesPersonDocumentIdNullWhenPersonOrDocumentIdMissing() {
        User userWithoutPerson = User.builder()
                .username("u1")
                .password("pwd")
                .enabled(true)
                .roles(Collections.singleton("STAFF"))
                .build();

        User userWithPersonWithoutDocument = User.builder()
                .username("u2")
                .password("pwd")
                .enabled(true)
                .roles(Collections.singleton("STAFF"))
                .person(Person.builder().build())
                .build();

        assertNull(UserMapper.map(userWithoutPerson).getPersonDocumentId());
        assertNull(UserMapper.map(userWithPersonWithoutDocument).getPersonDocumentId());
    }
}
