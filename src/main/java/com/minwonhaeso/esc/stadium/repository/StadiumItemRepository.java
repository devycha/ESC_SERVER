package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StadiumItemRepository extends JpaRepository<StadiumItem, Long> {
    void deleteByStadiumIdAndId(Long stadiumId, Long itemId);
    List<StadiumItem> findAllByStadium(Stadium stadium);
}
