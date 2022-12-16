package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Time openTime;

    @NotNull
    @Column(name = "close_time", nullable = false)
    private Time closeTime;

    @Column(name = "star_avg")
    private Double starAvg;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Member member;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "reservations")
//    private List<Reservation> reservations;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "likes")
//    private List<Like> likes;

    @Builder.Default
    @OneToMany(mappedBy = "stadium")
    private List<StadiumItem> rentalStadiumItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "stadium")
    private List<StadiumImg> imgs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "stadium")
    private List<StadiumTag> tags = new ArrayList<>();


//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "reviews")
//    private List<Review> reviews;

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
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();
    }

    public void setAll(StadiumDto.UpdateStadiumRequest request) {
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
            this.openTime = request.getOpenTime();
        }

        if (request.getCloseTime() != null) {
            this.closeTime = request.getCloseTime();
        }
    }
}
