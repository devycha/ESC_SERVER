package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StadiumReservationRepository extends JpaRepository<StadiumReservation, Long> {
    Page<StadiumReservation> findAllByMemberOrderByReservingDateDesc(
            Member member, Pageable pageable);
    Page<StadiumReservation> findAllByMemberAndReservingDateAfterOrderByReservingDateDesc(
            Member member, LocalDate reservingDate, Pageable pageable);
    Page<StadiumReservation> findAllByStadiumOrderByReservingDateDesc(Stadium stadium, Pageable pageable);
    List<StadiumReservation> findAllByStadiumAndReservingDate(Stadium stadium, LocalDate reservingDate);
    Long countAllByMemberAndStadiumAndStatusIs(Member member, Stadium stadium, StadiumReservationStatus status);
}
