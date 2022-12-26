package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StadiumLikeRepository extends JpaRepository<StadiumLike, Long> {
    Optional<StadiumLike> findByMemberAndStadium(Member member, Stadium stadium);

    Page<StadiumLike> findAllByMember(Member member, Pageable pageable);

}
