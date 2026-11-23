package edu.fiu.cen4021.elevate.controller;

import edu.fiu.cen4021.elevate.model.AttendanceRecord;
import edu.fiu.cen4021.elevate.model.Employee;
import edu.fiu.cen4021.elevate.repository.EmployeeRepository;
import edu.fiu.cen4021.elevate.service.CheckInException;
import edu.fiu.cen4021.elevate.service.CheckInService;
import edu.fiu.cen4021.elevate.service.MembershipVerification;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Attendance & Check-In screen (Member 3): membership verification,
 * approve/deny check-in, and attendance recording for the selected member.
 */
@Controller
@RequestMapping("/members/{memberId}/check-in")
public class CheckInController {
    private static final String VIEW = "check-in";
    private static final String REDIRECT = "redirect:/members/{memberId}/check-in";

    private final CheckInService checkIn;
    private final EmployeeRepository employees;

    public CheckInController(CheckInService checkIn, EmployeeRepository employees) {
        this.checkIn = checkIn;
        this.employees = employees;
    }

    @GetMapping
    public String view(@PathVariable String memberId, Model model,
                       Authentication authentication, HttpServletResponse response) {
        model.addAttribute("employeeName", employeeName(authentication));
        model.addAttribute("requestedMemberId", memberId);

        Optional<MembershipVerification> found = checkIn.verify(memberId);
        if (found.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("memberFound", false);
            return VIEW;
        }

        MembershipVerification verification = found.get();
        String payment = verification.getMember().getPaymentStatus();
        String lastCheckIn = verification.getMember().getLastCheckIn();

        model.addAttribute("memberFound", true);
        model.addAttribute("member", verification.getMember());
        model.addAttribute("status", verification.getStatus());
        model.addAttribute("statusClass",
                CheckInService.ACTIVE.equals(verification.getStatus()) ? "pill-good" : "pill-bad");
        model.addAttribute("paymentClass",
                "Paid".equalsIgnoreCase(payment) ? "pill-good" : "pill-warn");
        model.addAttribute("lastCheckIn",
                lastCheckIn == null || lastCheckIn.isBlank() ? "No previous check-ins" : lastCheckIn);
        model.addAttribute("warnings", verification.getWarnings());
        model.addAttribute("canApprove", verification.isApproveAllowed());
        model.addAttribute("approveHint", verification.getApproveBlockedReason());
        model.addAttribute("suggestedReason", verification.getSuggestedDenialReason());
        model.addAttribute("denialReasons", CheckInService.DENIAL_REASONS);
        model.addAttribute("openVisitId", verification.getOpenVisitId());
        model.addAttribute("history", verification.getHistory());
        return VIEW;
    }

    @PostMapping("/approve")
    public String approve(@PathVariable String memberId, Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            AttendanceRecord record = checkIn.approve(memberId, authentication.getName());
            result(redirect, "approved", record.getMemberName() + " was checked in at "
                    + record.getCheckInText() + ". The visit has been recorded.");
        } catch (CheckInException e) {
            result(redirect, "error", e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/deny")
    public String deny(@PathVariable String memberId,
                       @RequestParam(defaultValue = "") String reason,
                       @RequestParam(defaultValue = "") String notes,
                       Authentication authentication, RedirectAttributes redirect) {
        try {
            AttendanceRecord record = checkIn.deny(memberId, reason, notes, authentication.getName());
            result(redirect, "denied", "Check-in for " + record.getMemberName()
                    + " was denied. Reason: " + record.getReason() + ". The attempt has been recorded.");
        } catch (CheckInException e) {
            result(redirect, "error", e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/{recordId}/check-out")
    public String checkOut(@PathVariable String memberId, @PathVariable Long recordId,
                           RedirectAttributes redirect) {
        try {
            AttendanceRecord record = checkIn.checkOut(memberId, recordId);
            result(redirect, "checkout", record.getMemberName() + " was checked out at "
                    + record.getCheckOutText() + ".");
        } catch (CheckInException e) {
            result(redirect, "error", e.getMessage());
        }
        return REDIRECT;
    }

    /** Passes the outcome to the screen shown after the redirect. */
    private static void result(RedirectAttributes redirect, String type, String message) {
        redirect.addFlashAttribute("resultType", type);
        redirect.addFlashAttribute("resultMessage", message);
    }

    private String employeeName(Authentication authentication) {
        String username = authentication.getName();
        return employees.findByUsernameIgnoreCase(username)
                .map(Employee::getFullName).orElse(username);
    }
}
