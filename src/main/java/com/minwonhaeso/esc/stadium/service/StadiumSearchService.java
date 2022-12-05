package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import com.minwonhaeso.esc.stadium.repository.StadiumSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StadiumSearchService {
    private final StadiumSearchRepository stadiumSearchRepository;

    public StadiumDocument save(StadiumDocument stadiumDocument) {
        return stadiumSearchRepository.save(stadiumDocument);
    }

    public void delete(StadiumDocument stadiumDocument) {
        stadiumSearchRepository.delete(stadiumDocument);
    }


}
