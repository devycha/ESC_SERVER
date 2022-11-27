package com.minwonhaeso.esc.stadium.entity;

import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@ToString
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
    private Long weekdayPricePerHalfHour;

    @NotNull
    @Column(name = "holiday_price_per_half_hour", nullable = false)
    private Long holidayPricePerHalfHour;

    @NotNull
    @Column(name = "open_time", nullable = false)
    private Time openTime;

    @NotNull
    @Column(name = "close_time", nullable = false)
    private Time closeTime;

    @Column(name = "star_avg")
    private Double starAvg;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "reservations")
//    private List<Reservation> reservations;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "likes")
//    private List<Like> likes;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "rental_items")
//    private List<Item> rentalItems;

    @OneToMany(mappedBy = "stadium")
    private List<StadiumImg> imgs;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "tags")
//    private List<Tag> tags;

//    @OneToMany(mappedBy = "stadium")
//    @Column(name = "reviews")
//    private List<Review> reviews;


    @PrePersist
    public void prePersist() {
        if (this.starAvg == null) {
            this.starAvg = 0.0;
        }

        if (this.imgs == null) {
            this.imgs = new ArrayList<>();
        }
    }

}
