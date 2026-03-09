package it.atrevisan.staffmanagement.repository;

import it.atrevisan.staffmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
    void deleteByUsername(String username);
    Optional<User> findByPersonDocumentId(String documentId);
}