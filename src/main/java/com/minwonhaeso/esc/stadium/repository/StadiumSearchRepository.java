package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumSearchRepository extends ElasticsearchRepository<StadiumDocument, Long> {
    Page<StadiumDocument> findByName(String name, Pageable pageable);
}
