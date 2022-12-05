package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class StadiumSearchRepositorySupport {
    private final ElasticsearchOperations elasticsearchOperations;
}
