package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StadiumReservationRepository extends JpaRepository<StadiumReservation, Long> {
    Page<StadiumReservation> findAllByMemberAndStatusAndStartDateAfter(
            Member member, String status, LocalDateTime today, Pageable pageable);
}
