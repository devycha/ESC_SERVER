package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StadiumTagRepository extends JpaRepository<StadiumTag, Long> {
    void deleteByStadiumIdAndName(Long stadiumId, String tagName);
    List<StadiumTag> findAllByStadium(Stadium stadium);
}
