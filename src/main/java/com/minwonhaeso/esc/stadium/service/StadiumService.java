package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.common.exception.ErrorCode;
import com.minwonhaeso.esc.common.exception.stadium.StadiumNotFoundException;
import com.minwonhaeso.esc.stadium.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.entity.Stadium;
import com.minwonhaeso.esc.stadium.entity.StadiumImg;
import com.minwonhaeso.esc.stadium.repository.StadiumImgRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.common.exception.ErrorCode.StadiumNotFound;

@RequiredArgsConstructor
@Service
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumImgRepository stadiumImgRepository;


    public CreateStadiumDto.Response createStadium(CreateStadiumDto.Request request) {
        Stadium stadium = Stadium.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .lat(request.getLat())
                .lnt(request.getLnt())
                .address(request.getAddress())
                .weekdayPricePerHalfHour(request.getWeekdayPricePerHalfHour())
                .holidayPricePerHalfHour(request.getHolidayPricePerHalfHour())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();

        List<StadiumImg> imgs = request.getImgs().stream().map(imgUrl -> StadiumImg.builder()
                .stadium(stadium).imgUrl(imgUrl).build()).collect(Collectors.toList());

        stadium.setImgs(imgs);

        stadiumRepository.save(stadium);
        stadiumImgRepository.saveAll(imgs);

        return CreateStadiumDto.Response.fromEntity(stadium);
    }

    public void deleteStadium(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new StadiumNotFoundException(
                        StadiumNotFound.getStatusCode(), StadiumNotFound.getErrorMessage()
                )
        );

        List<StadiumImg> imgs = stadiumImgRepository.findAllByStadium(stadium);
        stadiumImgRepository.deleteAll(imgs);
        stadiumRepository.delete(stadium);
    }
}
