package edu.fiu.cen4021.elevate;

import edu.fiu.cen4021.elevate.model.AttendanceRecord;
import edu.fiu.cen4021.elevate.repository.AttendanceRepository;
import edu.fiu.cen4021.elevate.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Membership verification, approve/deny check-in, and attendance recording.
 * Each test uses its own seeded member, so the tests do not depend on each
 * other or on the order they run in.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckInFlowTest {
    @Autowired MockMvc mvc;
    @Autowired AttendanceRepository attendance;
    @Autowired MemberRepository members;

    private List<AttendanceRecord> recordsFor(String memberId) {
        return attendance.findByMemberIdOrderByRecordedAtDescIdDesc(memberId);
    }

    @Test void activeMemberShowsNoWarningsAndCanBeApproved() throws Exception {
        mvc.perform(get("/members/1008/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(view().name("check-in"))
                .andExpect(model().attribute("status", "Active"))
                .andExpect(model().attribute("warnings", empty()))
                .andExpect(model().attribute("canApprove", true))
                .andExpect(content().string(containsString("Lucy Heartfilia")));
    }

    @Test void expiredMemberShowsWarningAndCannotBeApproved() throws Exception {
        mvc.perform(get("/members/1006/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("status", "Expired"))
                .andExpect(model().attribute("warnings", hasItem("Membership expired")))
                .andExpect(model().attribute("warnings", hasItem("Payment pending")))
                .andExpect(model().attribute("canApprove", false));
    }

    @Test void suspendedMemberShowsWarningAndCannotBeApproved() throws Exception {
        mvc.perform(get("/members/1003/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("status", "Suspended"))
                .andExpect(model().attribute("warnings", hasItem("Membership suspended")))
                .andExpect(model().attribute("canApprove", false));
    }

    @Test void paymentPendingIsAWarningButCanBeApproved() throws Exception {
        mvc.perform(get("/members/1013/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("status", "Active"))
                .andExpect(model().attribute("warnings", hasItem("Payment pending")))
                .andExpect(model().attribute("canApprove", true));
    }

    @Test void approveRecordsAttendanceAndUpdatesLastCheckIn() throws Exception {
        String before = members.findById("1004").orElseThrow().getLastCheckIn();

        mvc.perform(post("/members/1004/check-in/approve").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/1004/check-in"))
                .andExpect(flash().attribute("resultType", "approved"));

        List<AttendanceRecord> records = recordsFor("1004");
        assertEquals(1, records.size());
        AttendanceRecord record = records.get(0);
        assertEquals("Approved", record.getDecision());
        assertEquals("Active", record.getMembershipStatus());
        assertEquals("frontdesk", record.getEmployeeUsername());
        assertEquals("Front Desk Employee", record.getEmployeeName());
        assertNotNull(record.getCheckInTime());
        assertNull(record.getCheckOutTime());
        assertNotEquals(before, members.findById("1004").orElseThrow().getLastCheckIn());
    }

    @Test void approveIsRejectedForExpiredMember() throws Exception {
        mvc.perform(post("/members/1002/check-in/approve").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("resultType", "error"))
                .andExpect(flash().attribute("resultMessage",
                        "Check-in cannot be approved: membership is expired."));

        assertTrue(recordsFor("1002").isEmpty());
    }

    @Test void approveIsRejectedForSuspendedMember() throws Exception {
        mvc.perform(post("/members/1012/check-in/approve").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("resultType", "error"))
                .andExpect(flash().attribute("resultMessage",
                        "Check-in cannot be approved: membership is suspended."));

        assertTrue(recordsFor("1012").isEmpty());
    }

    @Test void denyRecordsTheAttemptWithItsReason() throws Exception {
        mvc.perform(post("/members/1010/check-in/deny").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK"))
                .param("reason", "Membership expired"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/1010/check-in"))
                .andExpect(flash().attribute("resultType", "denied"));

        List<AttendanceRecord> records = recordsFor("1010");
        assertEquals(1, records.size());
        AttendanceRecord record = records.get(0);
        assertEquals("Denied", record.getDecision());
        assertEquals("Membership expired", record.getReason());
        assertEquals("Expired", record.getMembershipStatus());
        assertNull(record.getCheckInTime());
    }

    @Test void denyWithoutReasonIsRejected() throws Exception {
        mvc.perform(post("/members/1011/check-in/deny").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK"))
                .param("reason", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("resultType", "error"))
                .andExpect(flash().attribute("resultMessage", "Select a reason for the denial."));

        assertTrue(recordsFor("1011").isEmpty());
    }

    @Test void denyWithOtherRequiresNotes() throws Exception {
        mvc.perform(post("/members/1011/check-in/deny").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK"))
                .param("reason", "Other").param("notes", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("resultType", "error"))
                .andExpect(flash().attribute("resultMessage",
                        "Enter notes when the reason is \"Other\"."));

        assertTrue(recordsFor("1011").isEmpty());
    }

    @Test void secondApprovalIsBlockedUntilCheckOut() throws Exception {
        // First approval succeeds.
        mvc.perform(post("/members/1009/check-in/approve").with(csrf())
                .with(user("manager").roles("MANAGER")))
                .andExpect(flash().attribute("resultType", "approved"));

        // The member is now checked in: the screen warns and blocks a second approval.
        mvc.perform(get("/members/1009/check-in").with(user("manager").roles("MANAGER")))
                .andExpect(model().attribute("warnings", hasItem("Member is already checked in")))
                .andExpect(model().attribute("canApprove", false));
        mvc.perform(post("/members/1009/check-in/approve").with(csrf())
                .with(user("manager").roles("MANAGER")))
                .andExpect(flash().attribute("resultType", "error"))
                .andExpect(flash().attribute("resultMessage", "This member is already checked in."));
        assertEquals(1, recordsFor("1009").size());

        // Recording the check-out closes the visit.
        Long recordId = recordsFor("1009").get(0).getId();
        mvc.perform(post("/members/1009/check-in/" + recordId + "/check-out").with(csrf())
                .with(user("manager").roles("MANAGER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("resultType", "checkout"));
        assertNotNull(recordsFor("1009").get(0).getCheckOutTime());

        // After check-out the member can be approved again.
        mvc.perform(get("/members/1009/check-in").with(user("manager").roles("MANAGER")))
                .andExpect(model().attribute("canApprove", true));
    }

    @Test void attendanceHistoryListsRecordedDecisions() throws Exception {
        mvc.perform(post("/members/1005/check-in/deny").with(csrf())
                .with(user("frontdesk").roles("FRONT_DESK"))
                .param("reason", "Other").param("notes", "Forgot membership card"))
                .andExpect(flash().attribute("resultType", "denied"));

        mvc.perform(get("/members/1005/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "Check-in denied: Other - Forgot membership card")));
        assertFalse(recordsFor("1005").isEmpty());
    }

    @Test void unknownMemberShowsNotFound() throws Exception {
        mvc.perform(get("/members/9999/check-in").with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isNotFound())
                .andExpect(view().name("check-in"))
                .andExpect(model().attribute("memberFound", false))
                .andExpect(content().string(containsString("Member not found")));
    }

    @Test void checkInScreenRedirectsUnauthenticatedUsers() throws Exception {
        mvc.perform(get("/members/1001/check-in"))
                .andExpect(status().is3xxRedirection());
    }

    @Test void approveWithoutLoginIsNotRecorded() throws Exception {
        mvc.perform(post("/members/1001/check-in/approve").with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(recordsFor("1001").isEmpty());
    }
}
