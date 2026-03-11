package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.enums.AbsenceReason;
import it.atrevisan.staffmanagement.mapper.AbsenceMapper;
import it.atrevisan.staffmanagement.model.Absence;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.AbsenceRepository;
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
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final PersonRepository personRepository;
    private final ContractService contractService;

    public List<AbsenceDTO> getByPerson(String documentId){

        return absenceRepository
                .findByPersonDocumentId(documentId)
                .stream()
                .map(AbsenceMapper::map)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createAbsence(String documentId, LocalDate date, AbsenceReason reason){

        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Person not found"));

        boolean hasActiveContract = contractService.hasActiveContract(person, date);

        if(!hasActiveContract){
            throw new IllegalStateException("No active contract for this date");
        }

        Absence absence = Absence.builder()
                .id(generateNewId())
                .person(person)
                .date(date)
                .reason(reason)
                .build();

        absenceRepository.save(absence);
    }

    private String generateNewId(){
        String uuid = UUID.randomUUID().toString();
        while(absenceRepository.existsById(uuid)){
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @Transactional
    public void deleteAbsence(String id){
        absenceRepository.deleteById(id);
    }
    public List<AbsenceDTO> getAbsencesByPerson(String personDocumentId) {
        return absenceRepository.findByPersonDocumentId(personDocumentId)
                .stream()
                .map(AbsenceMapper::map)
                .collect(Collectors.toList());
    }

    public List<AbsenceDTO> getAllAbsences() {

        return absenceRepository.findAll()
                .stream()
                .map(AbsenceMapper::map)
                .collect(Collectors.toList());
    }

    public void updateAbsence(String id, AbsenceDTO dto) {

        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Absence not found: " + id));

        absence.setDate(dto.getDate());
        absence.setReason(dto.getReason());

        absenceRepository.save(absence);
    }
}