package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.entity.Stadium;
import com.minwonhaeso.esc.stadium.entity.StadiumImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StadiumImgRepository extends JpaRepository<StadiumImg, Long> {
    List<StadiumImg> findAllByStadium(Stadium stadium);
}
