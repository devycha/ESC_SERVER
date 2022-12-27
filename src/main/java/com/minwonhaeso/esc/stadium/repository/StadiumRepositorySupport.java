package com.minwonhaeso.esc.stadium.repository;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import com.minwonhaeso.esc.stadium.model.type.StadiumStatus;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.minwonhaeso.esc.stadium.model.entity.QStadium.stadium;
import static com.minwonhaeso.esc.stadium.model.entity.QStadiumLike.stadiumLike;

@Repository
public class StadiumRepositorySupport extends QuerydslRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public StadiumRepositorySupport(JPAQueryFactory queryFactory) {
        super(Stadium.class);
        this.queryFactory = queryFactory;
    }

    public List<Stadium> getAllStadiumsNearLocation(Double lnt, Double lat, Pageable pageable) {
        return queryFactory
                .selectFrom(stadium)
                .where(stadium.status.eq(StadiumStatus.AVAILABLE))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .orderBy(Expressions.stringTemplate("ST_Distance_Sphere({0}, {1})",
                        Expressions.stringTemplate("POINT({0}, {1})", lnt, lat),
                        Expressions.stringTemplate("POINT({0}, {1})", stadium.lnt, stadium.lat)
                ).asc()).fetch();
    }
    public List<StadiumLike> getAllAvailableLikeStadium(Long memberId,Pageable pageable){
        return queryFactory
                .selectFrom(stadiumLike)
                .where(stadiumLike.stadium.status.eq(StadiumStatus.AVAILABLE))
                .where(stadiumLike.member.memberId.eq(memberId))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
    }
}
