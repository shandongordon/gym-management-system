package edu.fiu.cen4021.elevate.repository;

import edu.fiu.cen4021.elevate.model.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String> {
    @Query("SELECT m FROM Member m WHERE LOWER(m.memberId) = LOWER(:query) " +
           "OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY m.fullName")
    List<Member> searchByIdOrName(@Param("query") String query, Pageable pageable);
}
