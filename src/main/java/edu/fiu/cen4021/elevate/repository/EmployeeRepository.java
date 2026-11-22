package edu.fiu.cen4021.elevate.repository;

import edu.fiu.cen4021.elevate.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByUsernameIgnoreCase(String username);
}
