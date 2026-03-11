package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.model.Absence;

public class AbsenceMapper {

    private AbsenceMapper(){}

    public static AbsenceDTO map(Absence absence){

        return AbsenceDTO.builder()
                .id(absence.getId())
                .personDocumentId(absence.getPerson() != null
                        ? absence.getPerson().getDocumentId()
                        : null)
                .date(absence.getDate())
                .reason(absence.getReason())
                .createdBy(absence.getCreatedBy())
                .createdTime(absence.getCreatedTime())
                .updatedBy(absence.getUpdatedBy())
                .updatedTime(absence.getUpdatedTime())
                .build();
    }

}