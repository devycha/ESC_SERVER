package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class StadiumReservationDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateReservationRequest {
        private LocalDate reservingDate;
        List<String> reservingTimes;
        private int headCount;
        private List<ItemRequest> items;
        private String paymentType;
    }

    @Data
    @Builder
    @ApiModel(value = "예약 내역 체육관 정보 Response")
    public static class ReservationStadiumInfoResponse {
        private StadiumInfoResponseDto stadium;
        private String date;
        private String openTime;
        private String closeTime;
        private int pricePerHalfHour;
        private List<String> reservedTimes;
    }

    @Data
    @Builder
    @ApiModel(value = "예약 생성 정보 Response")
    public static class CreateReservationResponse {
        private Long reservationId;
        private Long stadiumId;
        private String stadiumName;
        private String date;
        private String openTime;
        private String closeTime;
        private int pricePerHalfHour;
        private List<ItemResponse> rentalItems;
        private List<String> reservedTimes;
    }

    @Data
    @Builder
    @ApiModel(value = "예약 상세 내역 정보 Response")
    public static class ReservationInfoResponse {
        private Long reservationId;
        private Long stadiumId;
        private String name;
        private String status;
        private MemberResponse member;
        private String reservingDate;
        private List<String> reservingTime;
        private int headCount;
        private int price;
        private String paymentType;
        private List<ItemResponse> items;

        public static ReservationInfoResponse fromEntity(StadiumReservation reservation) {
            reservation.getReservingTimes().sort((a, b) -> a.ordinal() - b.ordinal());

            return ReservationInfoResponse.builder()
                    .reservationId(reservation.getId())
                    .stadiumId(reservation.getStadium().getId())
                    .name(reservation.getStadium().getName())
                    .member(MemberResponse.fromEntity(reservation.getMember()))
                    .reservingDate(reservation.getReservingDate().toString())
                    .reservingTime(reservation.getReservingTimes().stream()
                            .map(ReservingTime::getTime)
                            .collect(Collectors.toList()))
                    .headCount(reservation.getHeadCount())
                    .price(reservation.getPrice())
                    .paymentType(reservation.getPaymentType().toString())
                    .items(ItemResponse.fromReservation(reservation))
                    .status(reservation.getStatus().toString())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "예약 내역 리스트 정보 Response")
    public static class ReservationResponse {
        private Long reservationId;
        private Long stadiumId;
        private String name;
        private String address;
        private String imgUrl;
        private Double starAvg;
        private String status;

        public static ReservationResponse fromEntity(StadiumReservation reservation) {
            Stadium stadium = reservation.getStadium();
            return ReservationResponse.builder()
                    .reservationId(reservation.getId())
                    .stadiumId(stadium.getId())
                    .name(stadium.getName())
                    .address(stadium.getAddress() + " " + stadium.getDetailAddress())
                    .imgUrl(stadium.getImgs().get(0).getImgUrl())
                    .starAvg(stadium.getStarAvg())
                    .status(reservation.getStatus().toString())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRequest {
        private Long itemId;
        private int count;
    }

    @Data
    @Builder
    public static class ItemResponse {
        private Long id;
        private String name;
        private String imgUrl;
        private int price;
        private int count;

        public static ItemResponse fromEntity(StadiumItem item) {
            return ItemResponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .imgUrl(item.getImgUrl())
                    .price(item.getPrice())
                    .build();
        }

        public static ItemResponse fromReservationItem(StadiumReservationItem item) {
            return ItemResponse.builder()
                    .id(item.getId())
                    .name(item.getItem().getName())
                    .imgUrl(item.getItem().getImgUrl())
                    .price(item.getPrice())
                    .count(item.getCount())
                    .build();
        }

        public static List<ItemResponse> fromReservation(StadiumReservation reservation) {
            return reservation.getItems().stream().map(reservationItem -> {
                StadiumItem item = reservationItem.getItem();
                return ItemResponse.builder()
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
    private static class MemberResponse {
        private Long id;
        private String nickname;
        private String email;

        public static MemberResponse fromEntity(Member member) {
            return MemberResponse.builder()
                    .id(member.getMemberId())
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .build();
        }
    }

    @Data
    @Builder
    public static class PriceResponse {
        private int price;
    }
}
