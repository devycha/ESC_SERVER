package com.minwonhaeso.esc.member.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
//    @Query("select m from Member m join fetch m.role a where m.name = :name")
//    Optional<Member> findByNameWithRole(String name);

    Optional<Member> findByName(String username);
    Optional<Member> findByEmail(String email);
}
