package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.mapper.PersonMapper;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.model.User;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import it.atrevisan.staffmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        if(personRepository.existsByDocumentId(dto.getDocumentId())){
            throw new IllegalStateException("Person already exists");
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
    }

//    @Transactional
//    public void updatePerson(PersonDTO dto){
//
//        Person person = personRepository.findByDocumentId(dto.getDocumentId())
//                .orElseThrow(() -> new RuntimeException("Person not found"));
//
//        person.setName(dto.getName());
//        person.setSurname(dto.getSurname());
//        person.setBirthDate(dto.getBirthDate());
//        person.setDocumentId(dto.getDocumentId());
//        person.setEmail(dto.getEmail());
//        person.setAddress(dto.getAddress());
//        person.setPhone(dto.getPhone());
//
//        if(dto.getUsername() != null){
//            User user = userRepository.findByUsername(dto.getUsername())
//                    .orElse(null);
//            person.setUser(user);
//        }else{
//            person.setUser(null);
//        }
//
//        personRepository.save(person);
//    }

    @Transactional
    public void updatePerson(PersonDTO dto){

        Person person = personRepository.findByDocumentId(dto.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Person not found"));

        person.setName(dto.getName());
        person.setSurname(dto.getSurname());
        person.setBirthDate(dto.getBirthDate());
        person.setEmail(dto.getEmail());
        person.setAddress(dto.getAddress());
        person.setPhone(dto.getPhone());

        personRepository.save(person);
    }

    @Transactional
    public void deletePerson(String documentId){

        userRepository.findByPersonDocumentId(documentId)
                .ifPresent(user -> {
                    user.setPerson(null);
                    userRepository.save(user);
                });

        personRepository.deleteByDocumentId(documentId);
    }

    @Transactional
    public void savePerson(@NotNull PersonDTO dto, boolean createUser, boolean deleteUser) {

        Optional<Person> existing = personRepository.findByDocumentId(dto.getDocumentId());
        boolean isCreate = !existing.isPresent();

        /*
         * CREATE OR UPDATE PERSON
         */
        if (isCreate) {
            createPerson(dto);
        } else {
            updatePerson(dto);
        }

        /*
         * USER MANAGEMENT
         */
        if (createUser && dto.getUsername() == null) {

            String username = generateUsername(dto.getName(), dto.getSurname());

            userService.createUser(
                    username,
                    username,
                    Collections.singleton("STAFF")
            );

            assignUserToPerson(username, dto.getDocumentId());
        }

        if (deleteUser && dto.getUsername() != null) {

            unassignUserToPerson(dto.getDocumentId());
            userService.deleteUser(dto.getUsername());
        }
    }

    private String generateUsername(String name, String surname) {

        String base = (name + "." + surname).toLowerCase();
        String username = base;

        int i = 1;

        while (userService.findByUsername(username).isPresent()) {
            username = base + i;
            i++;
        }

        return username;
    }

    @Transactional
    public void assignUserToPerson(@NotNull String username, @NotNull String documentId){

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Person not found: " + documentId));

        if(user.getPerson() != null){
            throw new IllegalStateException("User already assigned to another person");
        }

        if(userRepository.findByPersonDocumentId(documentId).isPresent()){
            throw new IllegalStateException("Person already has an assigned user");
        }

        user.setPerson(person);

        userRepository.save(user);
    }

    @Transactional
    public void unassignUserToPerson(@NotNull String documentId){

        User user = userRepository.findByPersonDocumentId(documentId)
                .orElse(null);

        if(user == null) return;

        user.setPerson(null);

        userRepository.save(user);
    }
}