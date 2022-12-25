package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import com.minwonhaeso.esc.stadium.repository.StadiumRepositorySupport;
import com.minwonhaeso.esc.stadium.repository.StadiumSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StadiumSearchService {
    private final StadiumSearchRepository stadiumSearchRepository;
    private final StadiumRepositorySupport stadiumRepositorySupport;

    @Transactional(readOnly = true)
    public Page<StadiumDocument> search(
            String searchValue,
            Pageable pageable) {
        return stadiumSearchRepository
                .findByNameContainsIgnoreCaseOrAddressContainsIgnoreCase(
                        searchValue, searchValue, pageable);
    }

    @Transactional(readOnly = true)
    public List<StadiumResponseDto> getAllStadiumsNearLocation(Double lnt, Double lat, Pageable pageable) {
        return stadiumRepositorySupport.getAllStadiumsNearLocation(lnt, lat, pageable)
                .stream().map(StadiumResponseDto::fromEntity).collect(Collectors.toList());
    }
}
