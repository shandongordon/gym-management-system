package edu.fiu.cen4021.elevate.config;

import edu.fiu.cen4021.elevate.model.Employee;
import edu.fiu.cen4021.elevate.model.Member;
import edu.fiu.cen4021.elevate.repository.EmployeeRepository;
import edu.fiu.cen4021.elevate.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Sample data for the CEN4021 class demonstration only.
 * The records are fictional anime characters, NOT real users or production data.
 * Data is inserted once, only when the corresponding table is empty, so a
 * teammate's future edits and recorded attendance are not overwritten on restart.
 */
@Configuration
public class DemoDataInitializer {

    @Bean
    public CommandLineRunner seedDemoData(EmployeeRepository employees,
                                         MemberRepository members,
                                         PasswordEncoder encoder) {
        return args -> {
            if (employees.count() == 0) {
                employees.saveAll(List.of(
                        new Employee("E001", "frontdesk", encoder.encode("Elevate123!"),
                                "Front Desk Employee", "FRONT_DESK", true),
                        new Employee("E002", "manager", encoder.encode("Manager123!"),
                                "Gym Manager", "MANAGER", true)
                ));
            }

            if (members.count() == 0) {
                members.saveAll(List.of(
                        member("1001", "Yuji Itadori", "yuji.itadori", "Monthly Membership", "Active", "2026-11-01", "2026-11-30", "Paid", "2026-11-20 08:42"),
                        member("1002", "Megumi Fushiguro", "megumi.fushiguro", "Annual Membership", "Expired", "2025-10-01", "2026-09-30", "Pending", "2026-09-28 19:15"),
                        member("1003", "Nobara Kugisaki", "nobara.kugisaki", "Premium Membership", "Suspended", "2026-06-01", "2027-05-31", "Paid", "2026-10-15 17:31"),
                        member("1004", "Satoru Gojo", "satoru.gojo", "Annual Membership", "Active", "2026-03-14", "2027-03-13", "Paid", "2026-11-21 06:45"),
                        member("1005", "Maki Zenin", "maki.zenin", "Monthly Membership", "Active", "2026-11-08", "2026-12-07", "Paid", "2026-11-19 12:09"),
                        member("1006", "Yuta Okkotsu", "yuta.okkotsu", "Monthly Membership", "Expired", "2026-09-01", "2026-09-30", "Pending", "2026-09-25 20:20"),
                        member("1007", "Natsu Dragneel", "natsu.dragneel", "Premium Membership", "Active", "2026-07-01", "2027-06-30", "Paid", "2026-11-21 07:18"),
                        member("1008", "Lucy Heartfilia", "lucy.heartfilia", "Annual Membership", "Active", "2026-01-10", "2027-01-09", "Paid", "2026-11-18 18:05"),
                        member("1009", "Erza Scarlet", "erza.scarlet", "Premium Membership", "Active", "2026-04-01", "2027-03-31", "Paid", "2026-11-20 16:20"),
                        member("1010", "Gray Fullbuster", "gray.fullbuster", "Monthly Membership", "Expired", "2026-08-01", "2026-08-31", "Pending", "2026-08-29 13:15"),
                        member("1011", "Wendy Marvell", "wendy.marvell", "Monthly Membership", "Active", "2026-11-15", "2026-12-14", "Paid", "2026-11-21 09:30"),
                        member("1012", "Juvia Lockser", "juvia.lockser", "Annual Membership", "Suspended", "2026-02-01", "2027-01-31", "Pending", "2026-10-25 11:02")
                ));
            }

            // Check-in test data (Member 3): an active member whose payment is pending.
            // Added by ID so databases seeded before this record existed also receive it.
            if (!members.existsById("1013")) {
                members.save(member("1013", "Gajeel Redfox", "gajeel.redfox", "Annual Membership", "Active", "2026-10-01", "2027-09-30", "Pending", null));
            }
        };
    }

    private static Member member(String id, String fullName, String emailPrefix,
                                 String plan, String status, String start, String expiry,
                                 String paymentStatus, String lastCheckIn) {
        String email = emailPrefix + "@example.test";
        String phone = String.format("305-555-%04d", Integer.parseInt(id));
        return new Member(id, fullName, email, phone, plan, status, start,
                          expiry, paymentStatus, lastCheckIn);
    }
}
