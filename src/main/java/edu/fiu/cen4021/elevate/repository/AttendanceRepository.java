package edu.fiu.cen4021.elevate.repository;

import edu.fiu.cen4021.elevate.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    /** A member's attendance history, newest first. */
    List<AttendanceRecord> findByMemberIdOrderByRecordedAtDescIdDesc(String memberId);

    /** The member's visit on the given date that has no check-out time yet, if any. */
    Optional<AttendanceRecord> findFirstByMemberIdAndDecisionAndAttendanceDateAndCheckOutTimeIsNullOrderByRecordedAtDesc(
            String memberId, String decision, LocalDate attendanceDate);
}
