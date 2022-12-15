package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StadiumReservationItemRepository extends JpaRepository<StadiumReservationItem, Long> {
    List<StadiumReservationItem> findAllByReservation(StadiumReservation reservation);
}
