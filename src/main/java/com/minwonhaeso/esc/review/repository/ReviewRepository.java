package com.minwonhaeso.esc.review.repository;

import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    Page<Review> findAll(Pageable pageable);
    Page<Review> findAllByStadium(Stadium stadium, Pageable pageable);
}
