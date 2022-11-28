package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.stadium.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.dto.UpdateStadiumDto;
import com.minwonhaeso.esc.stadium.entity.Stadium;
import com.minwonhaeso.esc.stadium.entity.StadiumImg;
import com.minwonhaeso.esc.stadium.repository.StadiumImgRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;


@RequiredArgsConstructor
@Service
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumImgRepository stadiumImgRepository;

    public Page<StadiumResponseDto> getAllStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable).map(StadiumResponseDto::fromEntity);
    }

    @Transactional
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

    @Transactional
    public void deleteStadium(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        List<StadiumImg> imgs = stadiumImgRepository.findAllByStadium(stadium);
        stadiumImgRepository.deleteAll(imgs);
        stadiumRepository.delete(stadium);
    }

    @Transactional
    public void addStadiumImg(Long stadiumId, String imgUrl) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        StadiumImg img = StadiumImg.builder().stadium(stadium).imgUrl(imgUrl).build();
        stadium.getImgs().add(img);
        stadiumImgRepository.save(img);
    }

    @Transactional
    public void deleteStadiumImg(Long stadiumId, String imgUrl) {
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new StadiumException(StadiumNotFound);
        }

        stadiumImgRepository.deleteByStadiumIdAndImgUrl(stadiumId, imgUrl);
    }

    public StadiumResponseDto updateStadiumInfo(Long stadiumId, UpdateStadiumDto.Request request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        stadium.update(request);
        stadiumRepository.save(stadium);
        return StadiumResponseDto.fromEntity(stadium);
    }
}
