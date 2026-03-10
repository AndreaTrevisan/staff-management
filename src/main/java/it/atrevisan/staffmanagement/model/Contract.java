package it.atrevisan.staffmanagement.model;

import it.atrevisan.staffmanagement.enums.JobRole;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract extends UpdatableEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobRole jobRole;

    private LocalDate licenseExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_document_id")
    private Person person;
}