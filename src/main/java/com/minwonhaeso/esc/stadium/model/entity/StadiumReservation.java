package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
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
    private LocalDateTime startDate;
    @Column(nullable = false)
    private LocalDateTime endDate;
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

}
