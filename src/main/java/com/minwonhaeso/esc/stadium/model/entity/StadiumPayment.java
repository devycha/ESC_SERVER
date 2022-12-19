package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@RedisHash("stadiumPayment")
@AllArgsConstructor
@Builder
public class StadiumPayment {
    //결제자
    @Id
    private String id;

    private Long stadiumId;

    private LocalDate date;

    private Integer price;

    private int headCount;

    private List<StadiumReservationDto.ItemResponse> items;

    private List<String> reservedTimes;

    @TimeToLive
    private Long expireDt;

}
