package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservationItem;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    public static class ReservationInfoResponse {
        private Long id;
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
    public static class Response {
        private Long id;
        private StadiumResponseDto stadium;
        private MemberResponse member;
        private LocalDate reservingDate;
        private List<String> reservingTimes;
        private int headCount;
        private int price;
        private String paymentType;
        private List<ItemResponse> items;

        public static Response fromEntity(StadiumReservation reservation) {
            return Response.builder()
                    .id(reservation.getId())
                    .stadium(StadiumResponseDto.fromEntity(reservation.getStadium()))
                    .member(MemberResponse.fromEntity(reservation.getMember()))
                    .reservingDate(reservation.getReservingDate())
                    .reservingTimes(reservation.getReservingTimes().stream()
                            .map(ReservingTime::getTime)
                            .collect(Collectors.toList()))
                    .headCount(reservation.getHeadCount())
                    .price(reservation.getPrice())
                    .paymentType(reservation.getPaymentType().toString())
                    .items(ItemResponse.fromReservation(reservation))
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
