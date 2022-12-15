package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import com.minwonhaeso.esc.stadium.repository.StadiumLikeRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StadiumLikeService {
    private final StadiumLikeRepository stadiumLikeRepository;
    private final StadiumRepository stadiumRepository;

    public Map<String, String> likes(Long stadiumId, String onOff, Member member) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumException(StadiumErrorCode.StadiumNotFound));
        Optional<StadiumLike> optionalLike = stadiumLikeRepository.findByMemberAndStadium(member, stadium);
        likeCD(onOff, member, stadium, optionalLike);

        return (Map<String, String>) new HashMap<>().put("successMessage","찜하기 반영 성공");
    }

    private void likeCD(String onOff, Member member, Stadium stadium, Optional<StadiumLike> optionalLike) {
        if (onOff.equals("ON")) {
            if (optionalLike.isEmpty()) {
                stadiumLikeRepository.save(new StadiumLike(member, stadium));
            } else {
                throw new StadiumException(StadiumErrorCode.LikeRequestAlreadyMatched);
            }
        }
        if (onOff.equals("OFF")) {
            if (optionalLike.isPresent()) {
                stadiumLikeRepository.delete(optionalLike.get());
            } else {
                throw new StadiumException(StadiumErrorCode.LikeRequestAlreadyMatched);
            }
        }
    }
}
