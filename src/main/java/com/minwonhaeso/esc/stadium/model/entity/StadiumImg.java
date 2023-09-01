package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.stadium.model.dto.StadiumImgDto;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stadium_img")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class StadiumImg implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Column(name = "img_id")
    private String imgId;

    @Column(name = "img_url", length = 1000)
    private String imgUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static StadiumImg fromRequest(StadiumImgDto request, Stadium stadium) {
        return StadiumImg.builder()
                .stadium(stadium)
                .imgId(request.getPublicId())
                .imgUrl(request.getImgUrl())
                .build();
    }

    public void setAll(StadiumImgDto request) {
        this.imgId = request.getPublicId();
        this.imgUrl = request.getImgUrl();
    }
}
