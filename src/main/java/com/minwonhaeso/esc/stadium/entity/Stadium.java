package com.minwonhaeso.esc.stadium.entity;

import com.minwonhaeso.esc.stadium.dto.UpdateStadiumDto;
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
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stadium_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull
    @Column(name = "lat", nullable = false)
    private Double lat;

    @NotNull
    @Column(name = "lnt", nullable = false)
    private Double lnt;

    @NotNull
    @Column(name = "address", nullable = false)
    private String address;

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

//    @ManyToOne
//    private Member member;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "reservations")
//    private List<Reservation> reservations;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "likes")
//    private List<Like> likes;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "rental_items")
//    private List<Item> rentalItems;

    @Builder.Default
    @OneToMany(mappedBy = "stadium")
    private List<StadiumImg> imgs = new ArrayList<>();

    @OneToMany(mappedBy = "stadium")
    @Column(name = "tags")
    private List<StadiumTag> tags;

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

    public void update(UpdateStadiumDto.Request request) {
        if (request.getName() != null) {
            this.setName(request.getName());
        }

        if (request.getPhone() != null) {
            this.setPhone(request.getPhone());
        }

        if (request.getLat() != null) {
            this.setLat(request.getLat());
        }

        if (request.getLnt() != null) {
            this.setLnt(request.getLnt());
        }

        if (request.getAddress() != null) {
            this.setAddress(request.getAddress());
        }

        if (request.getWeekdayPricePerHalfHour() != null) {
            this.setWeekdayPricePerHalfHour(request.getWeekdayPricePerHalfHour());
        }

        if (request.getHolidayPricePerHalfHour() != null) {
            this.setHolidayPricePerHalfHour(request.getHolidayPricePerHalfHour());
        }

        if (request.getOpenTime() != null) {
            this.setOpenTime(request.getOpenTime());
        }

        if (request.getCloseTime() != null) {
            this.setCloseTime(request.getCloseTime());
        }
    }
}
