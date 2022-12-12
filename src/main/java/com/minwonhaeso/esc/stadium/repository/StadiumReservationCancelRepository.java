package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.StadiumReservationCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumReservationCancelRepository extends JpaRepository<StadiumReservationCancel, Long> {
}
