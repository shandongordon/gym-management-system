package edu.fiu.cen4021.elevate.service;

import edu.fiu.cen4021.elevate.model.Employee;
import edu.fiu.cen4021.elevate.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {
    private final EmployeeRepository repository;

    public EmployeeUserDetailsService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return User.withUsername(employee.getUsername())
                .password(employee.getPasswordHash())
                .roles(employee.getRole())
                .disabled(!employee.isEnabled())
                .build();
    }
}
