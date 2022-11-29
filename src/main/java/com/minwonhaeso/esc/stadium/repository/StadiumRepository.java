package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.entity.Stadium;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    @Override
    Page<Stadium> findAll(Pageable pageable);
}
