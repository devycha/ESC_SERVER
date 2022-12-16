package com.minwonhaeso.esc.stadium.model.entity;


import com.minwonhaeso.esc.member.model.entity.Member;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stadium_like")
@Entity
public class StadiumLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "memberId")
    @ManyToOne
    private Member member;

    @JoinColumn(name = "stadiumId")
    @ManyToOne
    private Stadium stadium;

    public StadiumLike(Member member, Stadium stadium) {
        this.member = member;
        this.stadium = stadium;
    }
}
