package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.enums.JobRole;
import it.atrevisan.staffmanagement.model.Contract;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.ContractRepository;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private PersonRepository personRepository;

    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, personRepository);
    }

    @Test
    void getAllContractsMapsResults() {
        Contract c1 = Contract.builder().id("1").jobRole(JobRole.DOCTOR).startDate(LocalDate.now()).endDate(LocalDate.now()).build();
        Contract c2 = Contract.builder().id("2").jobRole(JobRole.NURSE).startDate(LocalDate.now()).endDate(LocalDate.now()).build();
        when(contractRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        assertEquals(2, contractService.getAllContracts().size());
    }

    @Test
    void getContractsByPersonThrowsWhenPersonNotFound() {
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> contractService.getContractsByPerson("DOC1"));
    }

    @Test
    void getContractsByPersonReturnsMappedContracts() {
        Person person = Person.builder().documentId("DOC1").build();
        Contract c1 = Contract.builder().id("1").person(person).jobRole(JobRole.DOCTOR).startDate(LocalDate.now()).endDate(LocalDate.now()).build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));
        when(contractRepository.findByPerson(person)).thenReturn(Collections.singletonList(c1));

        assertEquals(1, contractService.getContractsByPerson("DOC1").size());
    }

    @Test
    void createContractThrowsWhenDatesInvalid() {
        Person person = Person.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));

        ContractDTO dto = ContractDTO.builder().startDate(LocalDate.of(2025, 2, 2)).endDate(LocalDate.of(2025, 1, 1)).jobRole(JobRole.DOCTOR).build();
        assertThrows(IllegalArgumentException.class, () -> contractService.createContract("DOC1", dto));
    }

    @Test
    void createContractThrowsWhenOverlappingExists() {
        Person person = Person.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));
        when(contractRepository.findByPersonAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(Contract.builder().id("X").build()));

        ContractDTO dto = ContractDTO.builder().startDate(LocalDate.of(2025, 1, 1)).endDate(LocalDate.of(2025, 2, 1)).jobRole(JobRole.DOCTOR).build();
        assertThrows(IllegalStateException.class, () -> contractService.createContract("DOC1", dto));
    }

    @Test
    void createContractSavesWhenValid() {
        Person person = Person.builder().documentId("DOC1").build();
        when(personRepository.findByDocumentId("DOC1")).thenReturn(Optional.of(person));
        when(contractRepository.findByPersonAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(contractRepository.existsById(anyString())).thenReturn(false);

        ContractDTO dto = ContractDTO.builder().startDate(LocalDate.of(2025, 1, 1)).endDate(LocalDate.of(2025, 2, 1)).jobRole(JobRole.DOCTOR).build();
        contractService.createContract("DOC1", dto);

        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void updateContractThrowsWhenContractNotFound() {
        when(contractRepository.findById("C1")).thenReturn(Optional.empty());
        ContractDTO dto = ContractDTO.builder().startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1)).jobRole(JobRole.DOCTOR).build();

        assertThrows(IllegalStateException.class, () -> contractService.updateContract("C1", dto));
    }

    @Test
    void updateContractUpdatesWhenValidExcludingSelfOverlap() {
        Person person = Person.builder().documentId("DOC1").build();
        Contract existing = Contract.builder().id("C1").person(person).startDate(LocalDate.of(2025,1,1)).endDate(LocalDate.of(2025,1,2)).jobRole(JobRole.DOCTOR).build();
        when(contractRepository.findById("C1")).thenReturn(Optional.of(existing));
        when(contractRepository.findByPersonAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(existing));

        ContractDTO dto = ContractDTO.builder().startDate(LocalDate.of(2025, 1, 3)).endDate(LocalDate.of(2025, 2, 1)).jobRole(JobRole.NURSE).build();
        contractService.updateContract("C1", dto);

        assertEquals(JobRole.NURSE, existing.getJobRole());
        verify(contractRepository).save(existing);
    }

    @Test
    void deleteContractThrowsWhenMissing() {
        when(contractRepository.existsById("C1")).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> contractService.deleteContract("C1"));
    }

    @Test
    void deleteContractDeletesWhenExists() {
        when(contractRepository.existsById("C1")).thenReturn(true);
        contractService.deleteContract("C1");
        verify(contractRepository).deleteById("C1");
    }

    @Test
    void hasActiveContractReturnsTrueOnlyInRange() {
        Person person = Person.builder().documentId("DOC").build();
        Contract active = Contract.builder().startDate(LocalDate.of(2025,1,1)).endDate(LocalDate.of(2025,1,31)).build();
        when(contractRepository.findByPerson(person)).thenReturn(Collections.singletonList(active));

        assertTrue(contractService.hasActiveContract(person, LocalDate.of(2025, 1, 15)));
        assertFalse(contractService.hasActiveContract(person, LocalDate.of(2025, 2, 1)));
    }
}
