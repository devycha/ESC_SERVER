package com.minwonhaeso.esc.member.repository;

import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberEmailRepository extends JpaRepository<MemberEmail, Long> {
    Optional<MemberEmail> findByAuthKey(String authKey);
    Optional<MemberEmail> findByEmail(String email);
}
