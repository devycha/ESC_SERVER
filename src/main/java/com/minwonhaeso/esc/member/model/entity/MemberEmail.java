package com.minwonhaeso.esc.member.model.entity;

import com.minwonhaeso.esc.member.model.type.EmailAuthStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "memberEmail")
public class MemberEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "authKey")
    private String authKey;
    @Column(name = "expire_dt")
    private LocalDateTime expireDt;

    @Column(name = "validate_dt")
    private LocalDateTime validateDt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EmailAuthStatus status;

}
