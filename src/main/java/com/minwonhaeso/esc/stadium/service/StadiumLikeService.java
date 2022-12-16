package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumLikeRequestDto;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import com.minwonhaeso.esc.stadium.repository.StadiumLikeRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StadiumLikeService {
    private final StadiumLikeRepository stadiumLikeRepository;
    private final StadiumRepository stadiumRepository;

    public StadiumLikeRequestDto likes(Long stadiumId, Member member) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumException(StadiumErrorCode.StadiumNotFound));
        Optional<StadiumLike> optionalLike = stadiumLikeRepository.findByMemberAndStadium(member, stadium);
        StadiumLikeRequestDto dto = new StadiumLikeRequestDto();
        if (optionalLike.isEmpty()){
            stadiumLikeRepository.save(StadiumLike.builder()
                    .stadium(stadium)
                    .member(member)
                    .build());
            dto.setResult(true);
            return dto;
        }
        stadiumLikeRepository.delete(optionalLike.get());
        dto.setResult(false);
        return dto;
    }
}
