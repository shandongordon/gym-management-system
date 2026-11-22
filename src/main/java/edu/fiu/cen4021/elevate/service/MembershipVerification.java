package edu.fiu.cen4021.elevate.service;

import edu.fiu.cen4021.elevate.model.AttendanceRecord;
import edu.fiu.cen4021.elevate.model.Member;

import java.util.List;

/** Everything the Attendance & Check-In screen shows for one member. */
public class MembershipVerification {
    private final Member member;
    private final String status;
    private final List<String> warnings;
    private final boolean approveAllowed;
    private final String approveBlockedReason;
    private final String suggestedDenialReason;
    private final Long openVisitId;
    private final List<AttendanceRecord> history;

    public MembershipVerification(Member member, String status, List<String> warnings,
                                  boolean approveAllowed, String approveBlockedReason,
                                  String suggestedDenialReason, Long openVisitId,
                                  List<AttendanceRecord> history) {
        this.member = member;
        this.status = status;
        this.warnings = warnings;
        this.approveAllowed = approveAllowed;
        this.approveBlockedReason = approveBlockedReason;
        this.suggestedDenialReason = suggestedDenialReason;
        this.openVisitId = openVisitId;
        this.history = history;
    }

    public Member getMember() { return member; }
    /** Active, Expired, or Suspended, as of today. */
    public String getStatus() { return status; }
    public List<String> getWarnings() { return warnings; }
    public boolean isApproveAllowed() { return approveAllowed; }
    /** Why Approve Check-In is unavailable, or null when it is allowed. */
    public String getApproveBlockedReason() { return approveBlockedReason; }
    /** The denial reason to pre-select, or an empty string. */
    public String getSuggestedDenialReason() { return suggestedDenialReason; }
    /** ID of today's visit that has no check-out yet, or null. */
    public Long getOpenVisitId() { return openVisitId; }
    public List<AttendanceRecord> getHistory() { return history; }
}
