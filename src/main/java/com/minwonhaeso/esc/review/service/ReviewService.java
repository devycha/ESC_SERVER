package com.minwonhaeso.esc.review.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.repository.ReviewRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final StadiumRepository stadiumRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDto.Response> getAllReviewsByStadium(Long stadiumId, Pageable pageable) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumException(StadiumErrorCode.StadiumNotFound));

        return reviewRepository.findAllByStadium(stadium, pageable).map(ReviewDto.Response::fromEntity);
    }

}
