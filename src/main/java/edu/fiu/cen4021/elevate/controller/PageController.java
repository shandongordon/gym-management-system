package edu.fiu.cen4021.elevate.controller;

import edu.fiu.cen4021.elevate.model.Employee;
import edu.fiu.cen4021.elevate.model.Member;
import edu.fiu.cen4021.elevate.repository.EmployeeRepository;
import edu.fiu.cen4021.elevate.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PageController {
    private final EmployeeRepository employees;
    private final MemberService members;

    public PageController(EmployeeRepository employees, MemberService members) {
        this.employees = employees;
        this.members = members;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        addEmployee(model, authentication);
        return "dashboard";
    }

    @GetMapping("/members")
    public String members(Model model, Authentication authentication) {
        addEmployee(model, authentication);
        return "members";
    }

    /**
     * Integration handoff only. Member 3 can replace this with their membership
     * verification screen and check-in implementation.
     */
    @GetMapping("/members/{memberId}/selected")
    public String selected(@PathVariable String memberId, Model model, Authentication authentication) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        addEmployee(model, authentication);
        model.addAttribute("member", member);
        return "selected-member";
    }

    private void addEmployee(Model model, Authentication authentication) {
        String username = authentication.getName();
        String displayName = employees.findByUsernameIgnoreCase(username)
                .map(Employee::getFullName).orElse(username);
        model.addAttribute("employeeName", displayName);
    }
}
