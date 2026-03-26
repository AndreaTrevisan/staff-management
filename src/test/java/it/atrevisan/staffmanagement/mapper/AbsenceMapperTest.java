package it.atrevisan.staffmanagement.mapper;

import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.enums.AbsenceReason;
import it.atrevisan.staffmanagement.model.Absence;
import it.atrevisan.staffmanagement.model.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbsenceMapperTest {

    @Test
    void mapMapsAllFieldsWhenPersonIsPresent() {
        Person person = Person.builder().documentId("DOC9").build();
        Absence absence = Absence.builder()
                .id("A1")
                .person(person)
                .date(LocalDate.of(2025, 3, 10))
                .reason(AbsenceReason.SICK)
                .build();

        AbsenceDTO dto = AbsenceMapper.map(absence);

        assertEquals("A1", dto.getId());
        assertEquals("DOC9", dto.getPersonDocumentId());
        assertEquals(LocalDate.of(2025, 3, 10), dto.getDate());
        assertEquals(AbsenceReason.SICK, dto.getReason());
    }

    @Test
    void mapReturnsNullPersonDocumentIdWhenPersonIsMissing() {
        Absence absence = Absence.builder()
                .id("A2")
                .date(LocalDate.of(2025, 3, 10))
                .reason(AbsenceReason.OTHER)
                .build();

        AbsenceDTO dto = AbsenceMapper.map(absence);

        assertNull(dto.getPersonDocumentId());
    }
}
