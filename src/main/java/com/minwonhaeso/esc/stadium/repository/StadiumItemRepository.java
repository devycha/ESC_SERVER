package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.entity.StadiumItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumItemRepository extends JpaRepository<StadiumItem, Long> {
    void deleteByStadiumIdAndId(Long stadiumId, Long itemId);
}
