package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.stadium.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.dto.UpdateStadiumDto;
import com.minwonhaeso.esc.stadium.entity.Stadium;
import com.minwonhaeso.esc.stadium.entity.StadiumImg;
import com.minwonhaeso.esc.stadium.entity.StadiumTag;
import com.minwonhaeso.esc.stadium.repository.StadiumImgRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumTagRepository;
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
    private final StadiumTagRepository stadiumTagRepository;

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

        List<StadiumTag> tags = request.getTags().stream().map(tag -> StadiumTag.builder()
                .stadium(stadium).name(tag).build()).collect(Collectors.toList());

        stadium.setImgs(imgs);
        stadium.setTags(tags);
        stadiumRepository.save(stadium);
        stadiumImgRepository.saveAll(imgs);
        stadiumTagRepository.saveAll(tags);

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

    @Transactional
    public StadiumResponseDto updateStadiumInfo(Long stadiumId, UpdateStadiumDto.Request request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        stadium.update(request);
        stadiumRepository.save(stadium);
        return StadiumResponseDto.fromEntity(stadium);
    }

    @Transactional
    public void addStadiumTag(Long stadiumId, String tagName) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        StadiumTag tag = StadiumTag.builder().stadium(stadium).name(tagName).build();
        stadium.getTags().add(tag);
        stadiumTagRepository.save(tag);
    }

    @Transactional
    public void deleteStadiumTag(Long stadiumId, String tagName) {
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new StadiumException(StadiumNotFound);
        }

        stadiumTagRepository.deleteByStadiumIdAndName(stadiumId, tagName);
    }
}
