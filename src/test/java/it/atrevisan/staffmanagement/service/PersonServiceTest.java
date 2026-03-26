package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import it.atrevisan.staffmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService(personRepository, userRepository, userService);
    }

    @Test
    void getAllStaffReturnsMappedList() {
        Person person = Person.builder().documentId("DOC1").name("Mario").surname("Rossi").build();
        when(personRepository.findAll()).thenReturn(Collections.singletonList(person));

        assertEquals(1, personService.getAllStaff().size());
    }

    @Test
    void createPersonThrowsWhenAlreadyExists() {
        PersonDTO dto = PersonDTO.builder().documentId("DOC1").name("Mario").surname("Rossi").build();
        when(personRepository.existsByDocumentId("DOC1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> personService.createPerson(dto));
    }

    @Test
    void createPersonSavesWhenNotExists() {
        PersonDTO dto = PersonDTO.builder().documentId("DOC1").name("Mario").surname("Rossi").build();
        when(personRepository.existsByDocumentId("DOC1")).thenReturn(false);

        personService.createPerson(dto);

        verify(personRepository).save(any(Person.class));
    }

    @Test
    void updatePersonThrowsWhenMissing() {
        PersonDTO dto = PersonDTO.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> personService.updatePerson(dto));
    }

    @Test
    void updatePersonUpdatesAndSaves() {
        PersonDTO dto = PersonDTO.builder()
                .documentId("DOC1")
                .name("Nuovo")
                .surname("Cognome")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("a@b.com")
                .address("Addr")
                .phone("123")
                .build();
        Person person = Person.builder().documentId("DOC1").name("Old").surname("Old").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));

        personService.updatePerson(dto);

        assertEquals("Nuovo", person.getName());
        verify(personRepository).save(person);
    }

    @Test
    void deletePersonThrowsWhenMissing() {
        when(personRepository.existsByDocumentId("DOC1")).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> personService.deletePerson("DOC1"));
    }

    @Test
    void deletePersonUnassignsUserAndDeletesPerson() {
        User user = User.builder().username("mario").build();
        when(personRepository.existsByDocumentId("DOC1")).thenReturn(true);
        when(userRepository.findByPersonDocumentId("DOC1")).thenReturn(Optional.of(user));

        personService.deletePerson("DOC1");

        assertNull(user.getPerson());
        verify(userRepository).save(user);
        verify(personRepository).deleteByDocumentId("DOC1");
    }

    @Test
    void savePersonCreatesAndCreatesUserWhenRequested() {
        PersonDTO dto = PersonDTO.builder().documentId("DOC1").name("Mario").surname("Rossi").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.empty(), Optional.of(Person.builder().documentId("DOC1").build()));
        when(userService.userExists("mario.rossi")).thenReturn(false);
        when(userRepository.findByUsername("mario.rossi")).thenReturn(Optional.of(User.builder().username("mario.rossi").build()));
        when(userRepository.existsByPersonDocumentId("DOC1")).thenReturn(false);

        personService.savePerson(dto, true, false);

        verify(userService).createUser(eq("mario.rossi"), eq("mario.rossi"), any(Set.class));
    }

    @Test
    void savePersonUpdatesAndDeletesUserWhenRequested() {
        PersonDTO dto = PersonDTO.builder().documentId("DOC1").name("Mario").surname("Rossi").username("user1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(Person.builder().documentId("DOC1").build()));
        when(userRepository.findByPersonDocumentId("DOC1")).thenReturn(Optional.of(User.builder().username("user1").build()));

        personService.savePerson(dto, false, true);

        verify(userService).deleteUser("user1");
    }

    @Test
    void assignUserToPersonThrowsWhenUserMissing() {
        when(userRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> personService.assignUserToPerson("u", "DOC"));
    }

    @Test
    void assignUserToPersonThrowsWhenPersonMissing() {
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(User.builder().username("u").build()));
        when(personRepository.findByDocumentId("DOC")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> personService.assignUserToPerson("u", "DOC"));
    }

    @Test
    void assignUserToPersonThrowsWhenUserAlreadyAssigned() {
        User user = User.builder().username("u").person(Person.builder().documentId("D1").build()).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(personRepository.findByDocumentId("DOC")).thenReturn(Optional.of(Person.builder().documentId("DOC").build()));

        assertThrows(IllegalStateException.class, () -> personService.assignUserToPerson("u", "DOC"));
    }

    @Test
    void assignUserToPersonThrowsWhenPersonAlreadyHasUser() {
        User user = User.builder().username("u").build();
        Person person = Person.builder().documentId("DOC").build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(personRepository.findByDocumentId("DOC")).thenReturn(Optional.of(person));
        when(userRepository.existsByPersonDocumentId("DOC")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> personService.assignUserToPerson("u", "DOC"));
    }

    @Test
    void assignUserToPersonAssignsAndSaves() {
        User user = User.builder().username("u").build();
        Person person = Person.builder().documentId("DOC").build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(personRepository.findByDocumentId("DOC")).thenReturn(Optional.of(person));
        when(userRepository.existsByPersonDocumentId("DOC")).thenReturn(false);

        personService.assignUserToPerson("u", "DOC");

        assertEquals(person, user.getPerson());
        verify(userRepository).save(user);
    }

    @Test
    void unassignUserToPersonNoOpWhenNoUser() {
        when(userRepository.findByPersonDocumentId("DOC")).thenReturn(Optional.empty());

        personService.unassignUserToPerson("DOC");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unassignUserToPersonSavesWhenUserFound() {
        User user = User.builder().username("u").person(Person.builder().documentId("DOC").build()).build();
        when(userRepository.findByPersonDocumentId("DOC")).thenReturn(Optional.of(user));

        personService.unassignUserToPerson("DOC");

        assertNull(user.getPerson());
        verify(userRepository).save(user);
    }

    @Test
    void getPersonReturnsMappedOptional() {
        Person person = Person.builder().documentId("DOC").name("A").surname("B").build();
        when(personRepository.findByDocumentId("DOC")).thenReturn(Optional.of(person));

        assertTrue(personService.getPerson("DOC").isPresent());
        assertEquals("DOC", personService.getPerson("DOC").get().getDocumentId());
    }
}
