package it.atrevisan.staffmanagement.repository;

import it.atrevisan.staffmanagement.model.Absence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

    List<Absence> findByPersonDocumentId(String documentId);

}