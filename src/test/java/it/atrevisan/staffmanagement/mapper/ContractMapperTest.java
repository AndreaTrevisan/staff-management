package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.enums.JobRole;
import it.atrevisan.staffmanagement.model.Contract;
import it.atrevisan.staffmanagement.model.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractMapperTest {

    @Test
    void toDtoMapsAllFields() {
        Person person = Person.builder().documentId("DOC123").build();
        Contract contract = Contract.builder()
                .id("C1")
                .person(person)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .jobRole(JobRole.DOCTOR)
                .licenseExpiry(LocalDate.of(2026, 1, 1))
                .build();

        ContractDTO dto = ContractMapper.toDTO(contract);

        assertEquals("C1", dto.getId());
        assertEquals("DOC123", dto.getPersonDocumentId());
        assertEquals(JobRole.DOCTOR, dto.getJobRole());
        assertEquals(LocalDate.of(2025, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 31), dto.getEndDate());
        assertEquals(LocalDate.of(2026, 1, 1), dto.getLicenseExpiry());
    }

    @Test
    void toDtoMapsNullPersonDocumentIdWhenPersonIsNull() {
        Contract contract = Contract.builder()
                .id("C2")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .jobRole(JobRole.DOCTOR)
                .build();

        ContractDTO dto = ContractMapper.toDTO(contract);

        assertNull(dto.getPersonDocumentId());
    }

    @Test
    void toEntityAndUpdateEntityWorkCorrectly() {
        ContractDTO dto = ContractDTO.builder()
                .id("C3")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .jobRole(JobRole.DOCTOR)
                .licenseExpiry(LocalDate.of(2025, 2, 10))
                .build();

        Contract entity = ContractMapper.toEntity(dto);
        assertEquals("C3", entity.getId());
        assertEquals(JobRole.DOCTOR, entity.getJobRole());

        ContractDTO updateDto = ContractDTO.builder()
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .jobRole(JobRole.NURSE)
                .licenseExpiry(LocalDate.of(2025, 6, 1))
                .build();

        ContractMapper.updateEntity(updateDto, entity);

        assertEquals(LocalDate.of(2024, 2, 1), entity.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 31), entity.getEndDate());
        assertEquals(JobRole.NURSE, entity.getJobRole());
        assertEquals(LocalDate.of(2025, 6, 1), entity.getLicenseExpiry());
    }
}
