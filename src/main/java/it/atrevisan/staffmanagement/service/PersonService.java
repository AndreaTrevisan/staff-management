package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.mapper.PersonMapper;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import it.atrevisan.staffmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<PersonDTO> getAllStaff() {
        return personRepository.findAll()
                .stream()
                .map(PersonMapper::map)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createPerson(PersonDTO dto) {
        log.debug("Creating person [documentId={}]", dto.getDocumentId());

        if (personRepository.existsByDocumentId(dto.getDocumentId())) {
            throw new IllegalStateException("Person already exists with documentId " + dto.getDocumentId());
        }

        Person person = Person.builder()
                .documentId(dto.getDocumentId())
                .name(dto.getName())
                .surname(dto.getSurname())
                .birthDate(dto.getBirthDate())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .build();

        personRepository.save(person);
        log.info("Created person [documentId={}]", dto.getDocumentId());
    }

    @Transactional
    public void updatePerson(PersonDTO dto) {
        log.debug("Updating person [documentId={}]", dto.getDocumentId());

        Person person = personRepository.findByDocumentId(dto.getDocumentId())
                .orElseThrow(() -> new IllegalStateException("Person not found: " + dto.getDocumentId()));

        person.setName(dto.getName());
        person.setSurname(dto.getSurname());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setAddress(dto.getAddress());
        person.setPhone(dto.getPhone());

        personRepository.save(person);
        log.info("Updated person [documentId={}]", dto.getDocumentId());
    }

    @Transactional
    public void deletePerson(String documentId) {
        log.debug("Deleting person [documentId={}]", documentId);

        if (!personRepository.existsByDocumentId(documentId)) {
            throw new IllegalStateException("Person not found: " + documentId);
        }

        userRepository.findByPersonDocumentId(documentId).ifPresent(user -> {
            log.debug("Unassigning user [username={}] from person [documentId={}]", user.getUsername(), documentId);
            user.setPerson(null);
            userRepository.save(user);
        });

        personRepository.deleteByDocumentId(documentId);
        log.info("Deleted person [documentId={}]", documentId);
    }

    @Transactional
    public void savePerson(@NotNull PersonDTO dto, boolean createUser, boolean deleteUser) {
        log.debug("Saving person [documentId={}, createUser={}, deleteUser={}]",
                dto.getDocumentId(), createUser, deleteUser);

        Optional<Person> existing = personRepository.findByDocumentId(dto.getDocumentId());
        if (existing.isEmpty()) {
            createPerson(dto);
        } else {
            updatePerson(dto);
        }

        boolean hasUsername = dto.getUsername() != null && !dto.getUsername().isBlank();

        if (createUser && !hasUsername) {
            String username = generateUsername(dto.getName(), dto.getSurname());
            userService.createUser(username, username, Collections.singleton(String.valueOf(Roles.STAFF)));
            assignUserToPerson(username, dto.getDocumentId());
        }

        if (deleteUser && hasUsername) {
            unassignUserToPerson(dto.getDocumentId());
            userService.deleteUser(dto.getUsername());
        }

        log.info("Saved person [documentId={}, createUser={}, deleteUser={}]",
                dto.getDocumentId(), createUser, deleteUser);
    }

    @Transactional
    public void assignUserToPerson(@NotNull String username, @NotNull String documentId) {
        log.debug("Assigning user [username={}] to person [documentId={}]", username, documentId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + documentId));

        if (user.getPerson() != null) {
            throw new IllegalStateException("User already assigned to another person");
        }

        if (userRepository.existsByPersonDocumentId(documentId)) {
            throw new IllegalStateException("Person already has an assigned user");
        }

        user.setPerson(person);
        userRepository.save(user);
        log.info("Assigned user [username={}] to person [documentId={}]", username, documentId);
    }

    @Transactional
    public void unassignUserToPerson(@NotNull String documentId) {
        log.debug("Unassigning user from person [documentId={}]", documentId);

        User user = userRepository.findByPersonDocumentId(documentId).orElse(null);
        if (user == null) {
            return;
        }

        user.setPerson(null);
        userRepository.save(user);
        log.info("Unassigned user [username={}] from person [documentId={}]", user.getUsername(), documentId);
    }

    public Optional<PersonDTO> getPerson(String documentId) {
        return personRepository.findByDocumentId(documentId).map(PersonMapper::map);
    }

    private String generateUsername(String name, String surname) {
        String normalizedName = name == null ? "user" : name.trim().replaceAll("\\s+", "");
        String normalizedSurname = surname == null ? "" : surname.trim().replaceAll("\\s+", "");

        String base = (normalizedName + "." + normalizedSurname).toLowerCase();
        String username = base;
        int i = 1;

        while (userService.userExists(username)) {
            username = base + i;
            i++;
        }

        log.debug("Generated username [base={}, username={}]", base, username);
        return username;
    }
}
