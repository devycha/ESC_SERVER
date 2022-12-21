package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.TimeFormatNotAccepted;
import static com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus.AVAILABLE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stadium")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stadium_id", nullable = false)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String phone;

    @NotNull
    @Column(nullable = false)
    private Double lat;

    @NotNull
    @Column(nullable = false)
    private Double lnt;

    @NotNull
    @Column(nullable = false)
    private String address;

    @NotNull
    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @NotNull
    @Column(name = "weekday_price_per_half_hour", nullable = false)
    private Integer weekdayPricePerHalfHour;

    @NotNull
    @Column(name = "holiday_price_per_half_hour", nullable = false)
    private Integer holidayPricePerHalfHour;

    @NotNull
    @Column(name = "open_time", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservingTime openTime;

    @NotNull
    @Column(name = "close_time", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservingTime closeTime;

    @Column(name = "star_avg")
    private Double starAvg;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Member member;

    @Builder.Default
    @OneToMany(
            mappedBy = "stadium",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private List<StadiumItem> rentalStadiumItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "stadium",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private List<StadiumImg> imgs = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "stadium",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private List<StadiumTag> tags = new ArrayList<>();

    @OneToMany(
            mappedBy = "stadium",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    @Column(name = "reviews")
    private List<Review> reviews;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.starAvg == null) {
            this.starAvg = 0.0;
        }

        if (this.imgs == null) {
            this.imgs = new ArrayList<>();
        }
    }

    public static Stadium fromRequest(StadiumDto.CreateStadiumRequest request, Member member) {
        try {
            ReservingTime openTime = ReservingTime.findTime(request.getOpenTime());
            ReservingTime closeTime = ReservingTime.findTime(request.getCloseTime());

            return Stadium.builder()
                    .member(member)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .lat(request.getLat())
                    .lnt(request.getLnt())
                    .address(request.getAddress())
                    .detailAddress(request.getDetailAddress())
                    .weekdayPricePerHalfHour(request.getWeekdayPricePerHalfHour())
                    .holidayPricePerHalfHour(request.getHolidayPricePerHalfHour())
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .build();

        } catch (NullPointerException e) {
            throw new StadiumException(TimeFormatNotAccepted);
        }
    }

    public void setAll(StadiumDto.UpdateStadiumRequest request) {
        try {
            ReservingTime openTime = ReservingTime.findTime(request.getOpenTime());
            ReservingTime closeTime = ReservingTime.findTime(request.getCloseTime());

            if (request.getName() != null) {
                this.name = request.getName();
            }

            if (request.getPhone() != null) {
                this.phone = request.getPhone();
            }

            if (request.getLat() != null) {
                this.lat = request.getLat();
            }

            if (request.getLnt() != null) {
                this.lnt = request.getLnt();
            }

            if (request.getAddress() != null) {
                this.address = request.getAddress();
            }

            if (request.getWeekdayPricePerHalfHour() != null) {
                this.weekdayPricePerHalfHour = request.getWeekdayPricePerHalfHour();
            }

            if (request.getHolidayPricePerHalfHour() != null) {
                this.holidayPricePerHalfHour = request.getHolidayPricePerHalfHour();
            }

            if (request.getOpenTime() != null) {
                this.openTime = openTime;
            }

            if (request.getCloseTime() != null) {
                this.closeTime = closeTime;
            }

            this.tags = request.getTags().stream()
                    .map(tag -> StadiumTag.builder().stadium(this).name(tag).build())
                    .collect(Collectors.toList());

            this.imgs = request.getImgs().stream()
                    .map(img -> StadiumImg.builder()
                            .id(img.getId())
                            .stadium(this)
                            .imgId(img.getPublicId())
                            .imgUrl(img.getImgUrl())
                            .build())
                    .collect(Collectors.toList());

            this.rentalStadiumItems = request.getRentalItems().stream()
                    .map(item -> StadiumItem.builder()
                            .id(item.getId())
                            .stadium(this)
                            .imgId(item.getPublicId())
                            .imgUrl(item.getImgUrl())
                            .name(item.getName())
                            .price(item.getPrice())
                            .status(AVAILABLE)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new StadiumException(TimeFormatNotAccepted);
        }

    }
}