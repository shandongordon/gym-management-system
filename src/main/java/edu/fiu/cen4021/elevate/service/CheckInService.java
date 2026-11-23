package edu.fiu.cen4021.elevate.service;

import edu.fiu.cen4021.elevate.model.AttendanceRecord;
import edu.fiu.cen4021.elevate.model.Employee;
import edu.fiu.cen4021.elevate.model.Member;
import edu.fiu.cen4021.elevate.repository.AttendanceRepository;
import edu.fiu.cen4021.elevate.repository.EmployeeRepository;
import edu.fiu.cen4021.elevate.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Membership verification, approve/deny check-in, and attendance recording (Member 3).
 *
 * Rules:
 *  - A membership is Active, Expired, or Suspended. It also counts as Expired
 *    once its expiration date has passed, even if the stored status says Active.
 *  - Check-in can be approved only for an Active membership whose member is
 *    not already checked in today.
 *  - A pending payment is a warning only; the employee decides.
 *  - A denial needs a reason. The reason "Other" also needs notes.
 */
@Service
public class CheckInService {
    public static final String ACTIVE = "Active";
    public static final String EXPIRED = "Expired";
    public static final String SUSPENDED = "Suspended";

    public static final String REASON_EXPIRED = "Membership expired";
    public static final String REASON_SUSPENDED = "Membership suspended";
    public static final String REASON_PAYMENT = "Payment pending";
    public static final String REASON_ALREADY_IN = "Member is already checked in";
    public static final String REASON_ID_MISMATCH = "ID does not match member";
    public static final String REASON_OTHER = "Other";

    public static final List<String> DENIAL_REASONS = List.of(
            REASON_EXPIRED, REASON_SUSPENDED, REASON_PAYMENT,
            REASON_ALREADY_IN, REASON_ID_MISMATCH, REASON_OTHER);

    private static final int MAX_NOTES_LENGTH = 120;
    private static final DateTimeFormatter LAST_CHECK_IN_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    private final MemberRepository members;
    private final AttendanceRepository attendance;
    private final EmployeeRepository employees;

    public CheckInService(MemberRepository members, AttendanceRepository attendance,
                          EmployeeRepository employees) {
        this.members = members;
        this.attendance = attendance;
        this.employees = employees;
    }

    /** Verification details for the check-in screen, or empty when the member does not exist. */
    @Transactional(readOnly = true)
    public Optional<MembershipVerification> verify(String memberId) {
        return members.findById(memberId).map(this::buildVerification);
    }

    /** Approves the check-in and records the visit. */
    @Transactional
    public AttendanceRecord approve(String memberId, String employeeUsername) {
        Member member = requireMember(memberId);
        String status = effectiveStatus(member, LocalDate.now());
        if (!ACTIVE.equals(status)) {
            throw new CheckInException("Check-in cannot be approved: membership is "
                    + status.toLowerCase(Locale.US) + ".");
        }
        if (findOpenVisit(member.getMemberId()).isPresent()) {
            throw new CheckInException("This member is already checked in.");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = attendance.save(new AttendanceRecord(
                member.getMemberId(), member.getFullName(), AttendanceRecord.APPROVED, null,
                status, member.getPaymentStatus(),
                employeeUsername, employeeName(employeeUsername), now));

        member.setLastCheckIn(now.format(LAST_CHECK_IN_FORMAT));
        members.save(member);
        return record;
    }

    /** Denies the check-in and records the attempt with its reason. */
    @Transactional
    public AttendanceRecord deny(String memberId, String reason, String notes, String employeeUsername) {
        Member member = requireMember(memberId);
        String cleanReason = reason == null ? "" : reason.trim();
        String cleanNotes = notes == null ? "" : notes.trim();
        if (cleanNotes.length() > MAX_NOTES_LENGTH) {
            cleanNotes = cleanNotes.substring(0, MAX_NOTES_LENGTH);
        }
        if (!DENIAL_REASONS.contains(cleanReason)) {
            throw new CheckInException("Select a reason for the denial.");
        }
        if (REASON_OTHER.equals(cleanReason) && cleanNotes.isEmpty()) {
            throw new CheckInException("Enter notes when the reason is \"Other\".");
        }

        String reasonText = cleanNotes.isEmpty() ? cleanReason : cleanReason + " - " + cleanNotes;
        return attendance.save(new AttendanceRecord(
                member.getMemberId(), member.getFullName(), AttendanceRecord.DENIED, reasonText,
                effectiveStatus(member, LocalDate.now()), member.getPaymentStatus(),
                employeeUsername, employeeName(employeeUsername), LocalDateTime.now()));
    }

    /** Records the check-out time on a member's open visit. */
    @Transactional
    public AttendanceRecord checkOut(String memberId, Long recordId) {
        AttendanceRecord record = attendance.findById(recordId)
                .filter(r -> r.getMemberId().equalsIgnoreCase(memberId))
                .orElseThrow(() -> new CheckInException("Attendance record not found."));
        if (!record.isApproved()) {
            throw new CheckInException("Only an approved check-in can be checked out.");
        }
        if (record.getCheckOutTime() != null) {
            throw new CheckInException("This visit already has a check-out time.");
        }
        record.recordCheckOut(LocalDateTime.now());
        return attendance.save(record);
    }

    /**
     * The membership status as of the given day: the stored status, except that
     * an Active membership whose expiration date has passed counts as Expired.
     * A membership is valid through its expiration date.
     */
    public static String effectiveStatus(Member member, LocalDate today) {
        String stored = member.getMembershipStatus() == null ? "" : member.getMembershipStatus().trim();
        if (SUSPENDED.equalsIgnoreCase(stored)) {
            return SUSPENDED;
        }
        if (EXPIRED.equalsIgnoreCase(stored)) {
            return EXPIRED;
        }
        LocalDate expiration = parseDate(member.getExpirationDate());
        if (expiration != null && expiration.isBefore(today)) {
            return EXPIRED;
        }
        if (ACTIVE.equalsIgnoreCase(stored)) {
            return ACTIVE;
        }
        return stored.isEmpty() ? "Unknown" : stored;
    }

    private MembershipVerification buildVerification(Member member) {
        String status = effectiveStatus(member, LocalDate.now());
        boolean paymentPending = "Pending".equalsIgnoreCase(member.getPaymentStatus());
        Optional<AttendanceRecord> openVisit = findOpenVisit(member.getMemberId());

        List<String> warnings = new ArrayList<>();
        if (EXPIRED.equals(status)) {
            warnings.add(REASON_EXPIRED);
        }
        if (paymentPending) {
            warnings.add(REASON_PAYMENT);
        }
        if (SUSPENDED.equals(status)) {
            warnings.add(REASON_SUSPENDED);
        }
        if (!ACTIVE.equals(status) && !EXPIRED.equals(status) && !SUSPENDED.equals(status)) {
            warnings.add("Membership status is " + status);
        }
        if (openVisit.isPresent()) {
            warnings.add(REASON_ALREADY_IN);
        }

        String blockedReason = null;
        if (!ACTIVE.equals(status)) {
            blockedReason = "Approve Check-In is unavailable because the membership is "
                    + status.toLowerCase(Locale.US) + ".";
        } else if (openVisit.isPresent()) {
            blockedReason = "Approve Check-In is unavailable because the member is already checked in.";
        }

        String suggested = "";
        if (EXPIRED.equals(status)) {
            suggested = REASON_EXPIRED;
        } else if (SUSPENDED.equals(status)) {
            suggested = REASON_SUSPENDED;
        } else if (openVisit.isPresent()) {
            suggested = REASON_ALREADY_IN;
        } else if (paymentPending) {
            suggested = REASON_PAYMENT;
        }

        return new MembershipVerification(member, status, warnings,
                blockedReason == null, blockedReason, suggested,
                openVisit.map(AttendanceRecord::getId).orElse(null),
                attendance.findByMemberIdOrderByRecordedAtDescIdDesc(member.getMemberId()));
    }

    private Optional<AttendanceRecord> findOpenVisit(String memberId) {
        return attendance
                .findFirstByMemberIdAndDecisionAndAttendanceDateAndCheckOutTimeIsNullOrderByRecordedAtDesc(
                        memberId, AttendanceRecord.APPROVED, LocalDate.now());
    }

    private Member requireMember(String memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new CheckInException("Member not found."));
    }

    private String employeeName(String username) {
        return employees.findByUsernameIgnoreCase(username)
                .map(Employee::getFullName).orElse(username);
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
