package it.atrevisan.staffmanagement.service;

import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.enums.AbsenceReason;
import it.atrevisan.staffmanagement.mapper.AbsenceMapper;
import it.atrevisan.staffmanagement.model.Absence;
import it.atrevisan.staffmanagement.model.Person;
import it.atrevisan.staffmanagement.repository.AbsenceRepository;
import it.atrevisan.staffmanagement.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final PersonRepository personRepository;
    private final ContractService contractService;

    public List<AbsenceDTO> getByPerson(String documentId) {
        return getAbsencesByPerson(documentId);
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

    @Transactional
    public void createAbsence(String documentId, LocalDate date, AbsenceReason reason) {
        log.debug("Creating absence [documentId={}, date={}, reason={}]", documentId, date, reason);
        if (date == null) {
            throw new IllegalArgumentException("Absence date is required");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Absence reason is required");
        }

        Person person = personRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalStateException("Person not found: " + documentId));

        if (!contractService.hasActiveContract(person, date)) {
            throw new IllegalStateException("No active contract for this date: " + date);
        }

        Absence absence = Absence.builder()
                .id(generateNewId())
                .person(person)
                .date(date)
                .reason(reason)
                .build();

        absenceRepository.save(absence);
        log.info("Created absence [absenceId={}, documentId={}, date={}, reason={}]",
                absence.getId(), documentId, date, reason);
    }

    @Transactional
    public void updateAbsence(String id, AbsenceDTO dto) {
        log.debug("Updating absence [id={}, date={}, reason={}]", id, dto.getDate(), dto.getReason());
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Absence not found: " + id));

        absence.setDate(dto.getDate());
        absence.setReason(dto.getReason());

        absenceRepository.save(absence);
        log.info("Updated absence [id={}]", id);
    }

    @Transactional
    public void deleteAbsence(String id) {
        log.debug("Deleting absence [id={}]", id);
        if (!absenceRepository.existsById(id)) {
            throw new IllegalStateException("Absence not found: " + id);
        }
        absenceRepository.deleteById(id);
        log.info("Deleted absence [id={}]", id);
    }

    private String generateNewId() {
        String uuid = UUID.randomUUID().toString();
        while (absenceRepository.existsById(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
}
