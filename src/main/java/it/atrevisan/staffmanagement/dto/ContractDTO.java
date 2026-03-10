package it.atrevisan.staffmanagement.dto;

import it.atrevisan.staffmanagement.enums.JobRole;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO extends UpdatableDTO {
    private String id;
    private String personDocumentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private JobRole jobRole;
    private LocalDate licenseExpiry;
}