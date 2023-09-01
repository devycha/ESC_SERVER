package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.type.StadiumStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {

    @Override
    Page<Stadium> findAll(Pageable pageable);
    Page<Stadium> findByMemberAndStatus(Member member, StadiumStatus status, Pageable pageable);
    List<Stadium> findAllByMember(Member member);
    Page<Stadium> findByNameContainingOrAddressContaining(String name, String address, Pageable pageable);
}
