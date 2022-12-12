package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class StadiumReservationDto {
    @Data
    @Builder
    public static class Response {
        private Long id;
        private StadiumResponseDto stadium;
        private MemberResponseDto member;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int headCount;
        private int price;
        private String paymentType;
        private List<ItemResponseDto> items;

        public static Response fromEntity(StadiumReservation reservation) {
            return Response.builder()
                    .id(reservation.getId())
                    .stadium(StadiumResponseDto.fromEntity(reservation.getStadium()))
                    .member(MemberResponseDto.fromEntity(reservation.getMember()))
                    .startDate(reservation.getStartDate())
                    .endDate(reservation.getEndDate())
                    .headCount(reservation.getHeadCount())
                    .price(reservation.getPrice())
                    .paymentType(reservation.getPaymentType().toString())
                    .items(ItemResponseDto.fromReservation(reservation))
                    .build();
        }
    }

    @Data
    @Builder
    private static class ItemResponseDto {
        private Long id;
        private String name;
        private String imgUrl;
        private int price;
        private int count;

        public static List<ItemResponseDto> fromReservation(StadiumReservation reservation) {
            return reservation.getItems().stream().map(reservationItem -> {
                StadiumItem item = reservationItem.getItem();
                return ItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .imgUrl(item.getImgUrl())
                        .price(item.getPrice())
                        .count(reservationItem.getCount())
                        .build();
            }).collect(Collectors.toList());
        }
    }

    @Data
    @Builder
    private static class MemberResponseDto {
        private Long id;
        private String nickname;
        private String email;

        public static MemberResponseDto fromEntity(Member member) {
            return MemberResponseDto.builder()
                    .id(member.getMemberId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .build();
        }
    }
}
