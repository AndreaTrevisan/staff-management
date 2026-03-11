package it.atrevisan.staffmanagement.model;

import it.atrevisan.staffmanagement.enums.AbsenceReason;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "absences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence extends UpdatableEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceReason reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_document_id")
    private Person person;
}