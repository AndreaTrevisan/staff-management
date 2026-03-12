package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.mapper.ContractMapper;
import it.atrevisan.staffmanagement.model.Contract;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.ContractRepository;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final PersonRepository personRepository;

    public List<ContractDTO> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(ContractMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ContractDTO> getContractsByPerson(String documentId) {
        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + documentId));

        return contractRepository.findByPerson(person)
                .stream()
                .map(ContractMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createContract(String documentId, ContractDTO dto) {
        log.debug("Creating contract [documentId={}, startDate={}, endDate={}, role={}]",
                documentId, dto.getStartDate(), dto.getEndDate(), dto.getJobRole());

        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + documentId));

        validateContractDates(person, dto.getStartDate(), dto.getEndDate());

        Contract contract = ContractMapper.toEntity(dto);
        contract.setId(generateNewId());
        contract.setPerson(person);

        contractRepository.save(contract);
        log.info("Created contract [contractId={}, documentId={}]", contract.getId(), documentId);
    }

    @Transactional
    public void updateContract(String contractId, ContractDTO dto) {
        log.debug("Updating contract [contractId={}, startDate={}, endDate={}, role={}]",
                contractId, dto.getStartDate(), dto.getEndDate(), dto.getJobRole());

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalStateException("Contract not found: " + contractId));

        Person person = contract.getPerson();
        validateContractDates(person, dto.getStartDate(), dto.getEndDate(), contractId);

        ContractMapper.updateEntity(dto, contract);
        contractRepository.save(contract);
        log.info("Updated contract [contractId={}]", contractId);
    }

    @Transactional
    public void deleteContract(String contractId) {
        log.debug("Deleting contract [contractId={}]", contractId);
        if (!contractRepository.existsById(contractId)) {
            throw new IllegalStateException("Contract not found: " + contractId);
        }
        contractRepository.deleteById(contractId);
        log.info("Deleted contract [contractId={}]", contractId);
    }

    public boolean hasActiveContract(@NotNull Person person, @NotNull LocalDate date) {
        return contractRepository.findByPerson(person)
                .stream()
                .anyMatch(c -> !c.getStartDate().isAfter(date) && !c.getEndDate().isBefore(date));
    }

    private void validateContractDates(Person person, LocalDate startDate, LocalDate endDate) {
        validateContractDates(person, startDate, endDate, null);
    }

    private void validateContractDates(Person person, LocalDate startDate, LocalDate endDate, String excludeContractId) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        List<Contract> overlappingContracts = contractRepository
                .findByPersonAndStartDateLessThanEqualAndEndDateGreaterThanEqual(person, endDate, startDate);

        if (excludeContractId != null) {
            overlappingContracts.removeIf(c -> c.getId().equals(excludeContractId));
        }

        if (!overlappingContracts.isEmpty()) {
            throw new IllegalStateException("There is already an active contract for this period");
        }
    }

    private String generateNewId() {
        String uuid = UUID.randomUUID().toString();
        while (contractRepository.existsById(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
}
