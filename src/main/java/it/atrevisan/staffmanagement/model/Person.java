package it.atrevisan.staffmanagement.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Person extends UpdatableEntity {

    @Id
    private String documentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private LocalDate birthDate;

    private String email;
    private String address;
    private String phone;

    @OneToOne(mappedBy = "person")
    @JoinColumn(unique = true)
    private User user;

}