package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.enums.AbsenceReason;
import it.atrevisan.staffmanagement.model.Absence;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.AbsenceRepository;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private ContractService contractService;

    private AbsenceService absenceService;

    @BeforeEach
    void setUp() {
        absenceService = new AbsenceService(absenceRepository, personRepository, contractService);
    }

    @Test
    void getByPersonDelegatesToGetAbsencesByPerson() {
        Absence absence = Absence.builder().id("A1").date(LocalDate.now()).reason(AbsenceReason.SICK).build();
        when(absenceRepository.findByPersonDocumentId("DOC1")).thenReturn(Collections.singletonList(absence));

        assertEquals(1, absenceService.getByPerson("DOC1").size());
        assertEquals(1, absenceService.getAbsencesByPerson("DOC1").size());
    }

    @Test
    void getAllAbsencesReturnsMappedList() {
        Absence absence = Absence.builder().id("A1").date(LocalDate.now()).reason(AbsenceReason.SICK).build();
        when(absenceRepository.findAll()).thenReturn(Collections.singletonList(absence));

        assertEquals(1, absenceService.getAllAbsences().size());
    }

    @Test
    void createAbsenceValidatesRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> absenceService.createAbsence("DOC1", null, AbsenceReason.SICK));
        assertThrows(IllegalArgumentException.class, () -> absenceService.createAbsence("DOC1", LocalDate.now(), null));
    }

    @Test
    void createAbsenceThrowsWhenPersonMissing() {
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> absenceService.createAbsence("DOC1", LocalDate.now(), AbsenceReason.SICK));
    }

    @Test
    void createAbsenceThrowsWhenNoActiveContract() {
        Person person = Person.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));
        when(contractService.hasActiveContract(person, LocalDate.of(2025, 2, 1))).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> absenceService.createAbsence("DOC1", LocalDate.of(2025, 2, 1), AbsenceReason.SICK));
    }

    @Test
    void createAbsenceSavesWhenValid() {
        Person person = Person.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));
        when(contractService.hasActiveContract(person, LocalDate.of(2025, 2, 1))).thenReturn(true);
        when(absenceRepository.existsById(anyString())).thenReturn(false);

        absenceService.createAbsence("DOC1", LocalDate.of(2025, 2, 1), AbsenceReason.SICK);

        verify(absenceRepository).save(any(Absence.class));
    }

    @Test
    void updateAbsenceThrowsWhenMissing() {
        when(absenceRepository.findById("A1")).thenReturn(Optional.empty());

        AbsenceDTO dto = AbsenceDTO.builder().date(LocalDate.now()).reason(AbsenceReason.VACATION).build();
        assertThrows(IllegalStateException.class, () -> absenceService.updateAbsence("A1", dto));
    }

    @Test
    void updateAbsenceSavesWhenFound() {
        Absence existing = Absence.builder().id("A1").date(LocalDate.now()).reason(AbsenceReason.SICK).build();
        when(absenceRepository.findById("A1")).thenReturn(Optional.of(existing));

        AbsenceDTO dto = AbsenceDTO.builder().date(LocalDate.of(2025, 8, 1)).reason(AbsenceReason.TRAINING).build();
        absenceService.updateAbsence("A1", dto);

        assertEquals(AbsenceReason.TRAINING, existing.getReason());
        verify(absenceRepository).save(existing);
    }

    @Test
    void deleteAbsenceThrowsWhenMissing() {
        when(absenceRepository.existsById("A1")).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> absenceService.deleteAbsence("A1"));
    }

    @Test
    void deleteAbsenceDeletesWhenExists() {
        when(absenceRepository.existsById("A1")).thenReturn(true);
        absenceService.deleteAbsence("A1");
        verify(absenceRepository).deleteById("A1");
    }
}
