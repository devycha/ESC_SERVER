package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.repository.StadiumRepositorySupport;
import com.minwonhaeso.esc.stadium.repository.StadiumSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.LatLntInvalid;

@RequiredArgsConstructor
@Service
public class StadiumSearchService {
    private final StadiumSearchRepository stadiumSearchRepository;
    private final StadiumRepositorySupport stadiumRepositorySupport;

    @Transactional(readOnly = true)
    public Page<StadiumResponseDto> search(
            String searchValue,
            Pageable pageable) {
        return stadiumSearchRepository
                .findByNameContainsIgnoreCaseOrAddressContainsIgnoreCase(
                        searchValue, searchValue, pageable)
                .map(StadiumResponseDto::fromDocument);
    }

    @Transactional(readOnly = true)
    public List<StadiumResponseDto> getAllStadiumsNearLocation(Double lnt, Double lat, Pageable pageable) {
        if (lat < -90 || lat > 90 || lnt < -180 || lnt > 180) {
            throw new StadiumException(LatLntInvalid);
        }

        return stadiumRepositorySupport.getAllStadiumsNearLocation(lnt, lat, pageable)
                .stream().map(StadiumResponseDto::fromEntity).collect(Collectors.toList());
    }
}
