package com.hambugi.cullecting.domain.member.repository;

import com.hambugi.cullecting.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Object> {
    boolean existsByEmail(String email);

    Member findByEmail(String email);
}
