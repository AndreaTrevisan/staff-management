package it.atrevisan.staffmanagement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PersonDTO extends UpdatableDTO {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String documentId;
    private String email;
    private String address;
    private String phone;
    private String username;
}
