package edu.fiu.cen4021.elevate.service;

import edu.fiu.cen4021.elevate.model.Member;
import edu.fiu.cen4021.elevate.repository.MemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    /** Exact member-ID match or case-insensitive partial full-name match. */
    public List<Member> search(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return List.of();
        }
        return repository.searchByIdOrName(rawQuery.trim(), PageRequest.of(0, 30));
    }

    public Optional<Member> findById(String memberId) {
        return repository.findById(memberId);
    }
}
