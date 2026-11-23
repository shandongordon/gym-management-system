package edu.fiu.cen4021.elevate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only for Member 2's prototype. Member 3 can add membership verification
 * and attendance relationships without changing the shared member ID.
 */
@Entity
@Table(name = "members")
public class Member {
    @Id
    @Column(name = "member_id", length = 12, nullable = false)
    private String memberId;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "email", length = 150, nullable = false)
    private String email;

    @Column(name = "phone_number", length = 24, nullable = false)
    private String phoneNumber;

    @Column(name = "membership_plan", length = 40, nullable = false)
    private String membershipPlan;

    @Column(name = "membership_status", length = 20, nullable = false)
    private String membershipStatus;

    @Column(name = "membership_start_date", length = 10, nullable = false)
    private String membershipStartDate;

    @Column(name = "expiration_date", length = 10, nullable = false)
    private String expirationDate;

    @Column(name = "payment_status", length = 20, nullable = false)
    private String paymentStatus;

    @Column(name = "last_check_in", length = 20)
    private String lastCheckIn;

    protected Member() { }

    public Member(String memberId, String fullName, String email, String phoneNumber,
                  String membershipPlan, String membershipStatus,
                  String membershipStartDate, String expirationDate,
                  String paymentStatus, String lastCheckIn) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.membershipPlan = membershipPlan;
        this.membershipStatus = membershipStatus;
        this.membershipStartDate = membershipStartDate;
        this.expirationDate = expirationDate;
        this.paymentStatus = paymentStatus;
        this.lastCheckIn = lastCheckIn;
    }

    public String getMemberId() { return memberId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMembershipPlan() { return membershipPlan; }
    public String getMembershipStatus() { return membershipStatus; }
    public String getMembershipStartDate() { return membershipStartDate; }
    public String getExpirationDate() { return expirationDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getLastCheckIn() { return lastCheckIn; }

    /** Updated by Member 3's check-in component when a check-in is approved. */
    public void setLastCheckIn(String lastCheckIn) { this.lastCheckIn = lastCheckIn; }
}
