package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.CreateReservationRequest;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stadium_reservation")
public class StadiumReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate reservingDate;

    @Builder.Default
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private List<ReservingTime> reservingTimes = new ArrayList<>();

    @Column(nullable = false)
    private int price;

    @ApiModelProperty(name = "인원수")
    private int headCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StadiumReservationStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "reservation")
    private List<StadiumReservationItem> items = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(name = "결제 타입")
    private PaymentType paymentType;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void executeReservation() {
        this.status = StadiumReservationStatus.EXECUTED;
    }

    public void cancelReservation() {
        this.status = StadiumReservationStatus.CANCELED;
    }

    public static StadiumReservation fromRequest(
            Stadium stadium,
            Member member,
            CreateReservationRequest request,
            int price
    ) {
        return StadiumReservation.builder()
                .stadium(stadium)
                .member(member)
                .reservingDate(request.getReservingDate())
                .reservingTimes(request.getReservingTimes().stream()
                        .map(time -> ReservingTime.findTime(time))
                        .collect(Collectors.toList()))
                .price(price)
                .headCount(request.getHeadCount())
                .status(StadiumReservationStatus.RESERVED)
                .paymentType(PaymentType.valueOf(request.getPaymentType()))
                .build();
    }
}
