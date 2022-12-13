package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationCancelRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationItemRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class StadiumReservationService {
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumReservationItemRepository stadiumReservationItemRepository;
    private final StadiumReservationCancelRepository stadiumReservationCancelRepository;

    public Page<StadiumReservationDto.Response> getAllReservationsByMember(Member member, Pageable pageable) {
        return stadiumReservationRepository
                .findAllByMemberAndStatusAndStartDateAfter(
                        member,
                        StadiumReservationStatus.RESERVED.toString(),
                        LocalDateTime.now(),
                        pageable).map(StadiumReservationDto.Response::fromEntity);
    }
}
