package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.mapper.ContractMapper;
import it.atrevisan.staffmanagement.model.Contract;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.ContractRepository;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RuntimeException("Person not found: " + documentId));

        return contractRepository.findByPerson(person)
                .stream()
                .map(ContractMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createContract(String documentId, ContractDTO dto) {
        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Person not found: " + documentId));

        validateContractDates(person, dto.getStartDate(), dto.getEndDate());

        Contract contract = ContractMapper.toEntity(dto);
        contract.setId(generateNewId());
        contract.setPerson(person);

        contractRepository.save(contract);
    }

    private String generateNewId(){
        String uuid = UUID.randomUUID().toString();
        while(contractRepository.existsById(uuid)){
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Transactional
    public void updateContract(String contractId, ContractDTO dto) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        Person person = contract.getPerson();
        validateContractDates(person, dto.getStartDate(), dto.getEndDate(), contractId);

        ContractMapper.updateEntity(dto, contract);

        contractRepository.save(contract);
    }

    @Transactional
    public void deleteContract(String contractId) {
        contractRepository.deleteById(contractId);
    }

    private void validateContractDates(Person person, LocalDate startDate, LocalDate endDate) {
        validateContractDates(person, startDate, endDate, null);
    }

    private void validateContractDates(Person person, LocalDate startDate, LocalDate endDate, String excludeContractId) {
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
}