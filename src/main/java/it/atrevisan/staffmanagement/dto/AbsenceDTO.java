package it.atrevisan.staffmanagement.dto;

import it.atrevisan.staffmanagement.enums.AbsenceReason;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AbsenceDTO extends UpdatableDTO {

    private String id;
    private String personDocumentId;
    private LocalDate date;
    private AbsenceReason reason;

}