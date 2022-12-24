package com.minwonhaeso.esc.review.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    Page<Review> findAll(Pageable pageable);
    Page<Review> findAllByStadium(Stadium stadium, Pageable pageable);
    Optional<Review> findByIdAndStadium(Long reviewId, Stadium stadium);
    Long countAllByMemberAndStadium(Member member, Stadium stadium);
    @Query(value = "select ROUND(AVG(star), 1) FROM Review GROUP BY stadium")
    Double findStarAvg(Stadium stadium);
}
