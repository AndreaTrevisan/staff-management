package it.atrevisan.staffmanagement.repository;

import it.atrevisan.staffmanagement.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByDocumentId(String documentId);
    void deleteByDocumentId(String documentId);
    Optional<Person> findByUserUsername(String username);
    boolean existsByDocumentId(String documentId);
}
