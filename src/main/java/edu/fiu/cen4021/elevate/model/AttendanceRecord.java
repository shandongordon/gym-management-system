package edu.fiu.cen4021.elevate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One check-in decision made by an employee for a member (Member 3).
 * An approved decision is a gym visit with a check-in time and, later, a
 * check-out time. A denied decision is kept as a record of the attempt.
 * The membership and payment status are stored as they were at decision time.
 */
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    public static final String APPROVED = "Approved";
    public static final String DENIED = "Denied";

    private static final DateTimeFormatter DATE_TEXT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_TEXT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    @Column(name = "member_id", length = 12, nullable = false)
    private String memberId;

    @Column(name = "member_name", length = 100, nullable = false)
    private String memberName;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "decision", length = 10, nullable = false)
    private String decision;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "membership_status", length = 20, nullable = false)
    private String membershipStatus;

    @Column(name = "payment_status", length = 20, nullable = false)
    private String paymentStatus;

    @Column(name = "employee_username", length = 80, nullable = false)
    private String employeeUsername;

    @Column(name = "employee_name", length = 100, nullable = false)
    private String employeeName;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    protected AttendanceRecord() { }

    public AttendanceRecord(String memberId, String memberName, String decision, String reason,
                            String membershipStatus, String paymentStatus,
                            String employeeUsername, String employeeName,
                            LocalDateTime recordedAt) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.decision = decision;
        this.reason = reason;
        this.membershipStatus = membershipStatus;
        this.paymentStatus = paymentStatus;
        this.employeeUsername = employeeUsername;
        this.employeeName = employeeName;
        this.recordedAt = recordedAt;
        this.attendanceDate = recordedAt.toLocalDate();
        this.checkInTime = APPROVED.equals(decision) ? recordedAt : null;
    }

    public void recordCheckOut(LocalDateTime time) {
        this.checkOutTime = time;
    }

    public Long getId() { return id; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public String getDecision() { return decision; }
    public String getReason() { return reason; }
    public String getMembershipStatus() { return membershipStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getEmployeeUsername() { return employeeUsername; }
    public String getEmployeeName() { return employeeName; }
    public LocalDateTime getRecordedAt() { return recordedAt; }

    public boolean isApproved() { return APPROVED.equals(decision); }

    // ----- text shown in the Attendance History table -----

    public String getDateText() { return attendanceDate.format(DATE_TEXT); }

    public String getCheckInText() {
        return checkInTime == null ? "—" : checkInTime.format(TIME_TEXT);
    }

    public String getCheckOutText() {
        return checkOutTime == null ? "—" : checkOutTime.format(TIME_TEXT);
    }

    public String getNotes() {
        if (isApproved()) {
            return "Approved by " + employeeName
                    + ("Pending".equalsIgnoreCase(paymentStatus) ? " (payment pending)" : "");
        }
        return "Check-in denied: " + reason + " (" + employeeName + ")";
    }
}
