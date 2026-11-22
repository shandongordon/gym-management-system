package edu.fiu.cen4021.elevate.controller;

import edu.fiu.cen4021.elevate.model.Member;
import edu.fiu.cen4021.elevate.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberRestController {
    private final MemberService service;

    public MemberRestController(MemberService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam(defaultValue = "") String query) {
        if (query.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please enter an ID or name before searching."));
        }
        List<Member> matches = service.search(query);
        return ResponseEntity.ok(matches.stream()
                .map(member -> Map.of(
                        "memberId", member.getMemberId(),
                        "fullName", member.getFullName(),
                        "email", member.getEmail(),
                        "phoneNumber", member.getPhoneNumber()
                ))
                .toList());
    }

    /** Full read-only record for Member 3's membership-verification component. */
    @GetMapping("/{memberId}")
    public Member findById(@PathVariable String memberId) {
        return service.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Member not found"));
    }
}
