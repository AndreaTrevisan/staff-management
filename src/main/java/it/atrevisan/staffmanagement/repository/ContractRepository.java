package it.atrevisan.staffmanagement.repository;

import it.atrevisan.staffmanagement.model.Contract;
import it.atrevisan.staffmanagement.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, String> {

    List<Contract> findByPerson(Person person);

    List<Contract> findByPersonAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Person person,
            LocalDate dateStart,
            LocalDate dateEnd
    );
}