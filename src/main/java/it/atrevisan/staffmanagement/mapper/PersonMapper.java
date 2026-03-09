package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.model.Person;

public class PersonMapper {

    private PersonMapper(){}

    public static PersonDTO map(Person person){

        return PersonDTO.builder()
                .name(person.getName())
                .surname(person.getSurname())
                .birthDate(person.getBirthDate())
                .documentId(person.getDocumentId())
                .email(person.getEmail())
                .address(person.getAddress())
                .phone(person.getPhone())
                .username(
                        person.getUser() != null ?
                                person.getUser().getUsername() : null
                )
                .createdTime(person.getCreatedTime())
                .createdBy(person.getCreatedBy())
                .updatedTime(person.getUpdatedTime())
                .updatedBy(person.getUpdatedBy())
                .build();
    }
}