package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.*;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto.CreateStadiumResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto.UpdateStadiumRequest;
import com.minwonhaeso.esc.stadium.model.dto.StadiumItemDto.CreateItemRequest;
import com.minwonhaeso.esc.stadium.model.dto.StadiumItemDto.CreateItemResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumItemDto.DeleteItemRequest;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.UnAuthorizedAccess;
import static com.minwonhaeso.esc.stadium.model.type.StadiumStatus.AVAILABLE;


@RequiredArgsConstructor
@Service
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumImgRepository stadiumImgRepository;
    private final StadiumTagRepository stadiumTagRepository;
    private final StadiumItemRepository stadiumItemRepository;
    private final StadiumSearchRepository stadiumSearchRepository;
    private final StadiumLikeRepository stadiumLikeRepository;

    @Transactional(readOnly = true)
    public StadiumInfoResponseDto getStadiumInfo(Long stadiumId, Member member) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        boolean isLike = member != null &&
                stadiumLikeRepository.existsByStadiumAndMember(stadium, member);

        System.out.println(member);

        return StadiumInfoResponseDto.fromEntity(stadium, isLike);
    }

    @Transactional(readOnly = true)
    public Page<StadiumResponseDto> getAllStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable).map(StadiumResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<StadiumResponseDto> getAllStadiumsByManager(Member member, Pageable pageable) {
        return stadiumRepository.findByMemberAndStatus(member, AVAILABLE, pageable).map(StadiumResponseDto::fromEntity);
    }

    @Transactional
    public CreateStadiumResponse createStadium(StadiumDto.CreateStadiumRequest request, Member member) {
        Stadium stadium = Stadium.fromRequest(request, member);

        if (request.getRentalItems().size() > 0) {
            List<StadiumItem> stadiumItems = request.getRentalItems()
                    .stream().map(item -> StadiumItem.fromRequest(item, stadium))
                    .collect(Collectors.toList());

            stadium.getRentalStadiumItems().addAll(stadiumItems);
            stadiumItemRepository.saveAll(stadiumItems);
        }

        if (request.getImgs().size() > 0) {
            List<StadiumImg> imgs = request.getImgs().stream().map(img -> StadiumImg.builder()
                    .stadium(stadium).imgId(img.getPublicId()).imgUrl(img.getImgUrl()).build())
                    .collect(Collectors.toList());

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

        return CreateStadiumResponse.fromEntity(stadium);
    }

    @Transactional
    public void deleteStadium(Member member, Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }
//        // 진짜 삭제
//        stadiumSearchRepository.deleteById(stadiumId);
//        stadiumRepository.delete(stadium);
        
        stadium.deleteStadium();
        stadiumRepository.save(stadium);
        stadiumSearchRepository.save(StadiumDocument.fromEntity(stadium));
    }

    public StadiumImgDto addStadiumImg(Member member, Long stadiumId, String imgUrl) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        StadiumImg img = StadiumImg.builder().stadium(stadium).imgUrl(imgUrl).build();
        stadium.getImgs().add(img);
        stadiumImgRepository.save(img);

        return StadiumImgDto
                .builder()
                .publicId(img.getImgId())
                .imgUrl(img.getImgUrl())
                .build();
    }

    public void deleteStadiumImg(Member member, Long stadiumId, String imgUrl) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        stadiumImgRepository.deleteByStadiumIdAndImgUrl(stadiumId, imgUrl);
    }

    @Transactional
    public StadiumInfoResponseDto updateStadiumInfo(Member member, Long stadiumId, UpdateStadiumRequest request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        stadiumTagRepository.deleteAll(stadium.getTags());
        stadiumImgRepository.deleteAll(stadium.getImgs());
        stadiumItemRepository.deleteAll(stadium.getRentalStadiumItems());
        stadium.setAll(request);
        System.out.println(request.getRentalItems());
        stadiumTagRepository.saveAll(stadium.getTags());
        stadiumImgRepository.saveAll(stadium.getImgs());
        stadiumItemRepository.saveAll(stadium.getRentalStadiumItems());
        stadiumRepository.save(stadium);
        StadiumDocument stadiumDocument = StadiumDocument.fromEntity(stadium);
        stadiumSearchRepository.save(stadiumDocument);
        boolean isLike = stadiumLikeRepository.existsByStadiumAndMember(stadium, member);
        return StadiumInfoResponseDto.fromEntity(stadium, isLike);
    }

    public StadiumTagDto addStadiumTag(Member member, Long stadiumId, String tagName) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        StadiumTag tag = StadiumTag.builder().stadium(stadium).name(tagName).build();
        stadium.getTags().add(tag);
        stadiumTagRepository.save(tag);

        return StadiumTagDto.builder().tagName(tag.getName()).build();
    }

    public void deleteStadiumTag(Member member, Long stadiumId, String tagName) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        stadiumTagRepository.deleteByStadiumIdAndName(stadiumId, tagName);
    }

    public CreateItemResponse addStadiumItem(Long stadiumId, CreateItemRequest request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound)
        );

        StadiumItem item = StadiumItem.fromRequest(request, stadium);
        stadium.getRentalStadiumItems().add(item);
        stadiumItemRepository.save(item);

        return CreateItemResponse.builder()
                .name(item.getName())
                .publicId(item.getImgId())
                .imgUrl(item.getImgUrl())
                .price(item.getPrice())
                .build();
    }

    public void deleteStadiumItem(Member member, Long stadiumId, DeleteItemRequest request) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (stadium.getMember().getMemberId() != member.getMemberId()) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        stadiumItemRepository.deleteByStadiumIdAndId(stadiumId, request.getId());
    }
}
