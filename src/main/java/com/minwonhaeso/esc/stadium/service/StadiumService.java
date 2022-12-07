package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.stadium.model.dto.CreateStadiumItemDto;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.dto.UpdateStadiumDto;
import com.minwonhaeso.esc.stadium.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;
import static com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus.AVAILABLE;


@RequiredArgsConstructor
@Service
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumImgRepository stadiumImgRepository;
    private final StadiumTagRepository stadiumTagRepository;
    private final StadiumItemRepository stadiumItemRepository;
    private final StadiumRepositorySupport stadiumRepositorySupport;
    private final StadiumSearchRepository stadiumSearchRepository;
    
    public Page<StadiumResponseDto> getAllStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable).map(StadiumResponseDto::fromEntity);
    }

    @Transactional
    public CreateStadiumDto.Response createStadium(CreateStadiumDto.Request request) {
        Stadium stadium = Stadium.fromRequest(request);

        if (request.getItems().size() > 0) {
            List<StadiumItem> stadiumItems = request.getItems()
                    .stream().map(item -> StadiumItem.fromRequest(stadium, item))
                    .collect(Collectors.toList());

            stadium.getRentalStadiumItems().addAll(stadiumItems);
            stadiumItemRepository.saveAll(stadiumItems);
        }

        if (request.getImgs().size() > 0) {
            List<StadiumImg> imgs = request.getImgs().stream().map(imgUrl -> StadiumImg.builder()
                    .stadium(stadium).imgUrl(imgUrl).build()).collect(Collectors.toList());

            stadium.getImgs().addAll(imgs);
            stadiumImgRepository.saveAll(imgs);
        }

        if (request.getTags().size() > 0) {
            List<StadiumTag> tags = request.getTags().stream().map(tag -> StadiumTag.builder()
                    .stadium(stadium).name(tag).build()).collect(Collectors.toList());
            stadium.getTags().addAll(tags);
            stadiumTagRepository.saveAll(tags);
        }

        stadiumRepository.save(stadium);
        stadiumSearchRepository.save(StadiumDocument.fromEntity(stadium));

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

    @Transactional
    public CreateStadiumItemDto.Response addStadiumItem(Long stadiumId, CreateStadiumItemDto.Request request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        StadiumItem item = StadiumItem.fromRequest(stadium, request);
        stadium.getRentalStadiumItems().add(item);
        stadiumItemRepository.save(item);

        return CreateStadiumItemDto.Response.builder()
                .name(item.getName())
                .imgUrl(item.getImgUrl())
                .price(item.getPrice())
                .cnt(item.getCnt())
                .isAvailable(item.getStatus() == AVAILABLE)
                .build();
    }

    @Transactional
    public void deleteStadiumItem(Long stadiumId, UpdateStadiumDto.DeleteItemRequest request) {
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new StadiumException(StadiumNotFound);
        }

        stadiumItemRepository.deleteByStadiumIdAndId(stadiumId, request.getItemId());
    }

    @Transactional(readOnly = true)
    public List<StadiumResponseDto> getAllStadiumsNearLocation(Double lnt, Double lat, Pageable pageable) {
        return stadiumRepositorySupport.getAllStadiumsNearLocation(lnt, lat, pageable)
                .stream().map(StadiumResponseDto::fromEntity).collect(Collectors.toList());
    }
}
