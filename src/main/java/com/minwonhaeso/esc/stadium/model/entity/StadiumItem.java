package com.minwonhaeso.esc.stadium.model.entity;

import com.minwonhaeso.esc.stadium.model.dto.StadiumItemDto;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus.AVAILABLE;
import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stadium_item")
@Entity
@ToString
@EntityListeners(AuditingEntityListener.class)
public class StadiumItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Column(name = "public_id")
    private String imgId;

    @Column(name = "img_url", length = 1000)
    private String imgUrl;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Enumerated(STRING)
    private StadiumItemStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static StadiumItem fromRequest(StadiumItemDto.CreateItemRequest request, Stadium stadium) {
        return StadiumItem.builder()
                .name(request.getName())
                .stadium(stadium)
                .imgId(request.getPublicId())
                .imgUrl(request.getImgUrl())
                .price(request.getPrice())
                .status(AVAILABLE)
                .build();
    }

    public static StadiumItem fromRequest(StadiumItemDto.UpdateItemRequest request, Stadium stadium) {
        return StadiumItem.builder()
                .name(request.getName())
                .stadium(stadium)
                .imgId(request.getPublicId())
                .imgUrl(request.getImgUrl())
                .price(request.getPrice())
                .status(AVAILABLE)
                .build();
    }

    public void setAll(StadiumItemDto.UpdateItemRequest request) {
        this.name = request.getName();
        this.imgId = request.getPublicId();
        this.imgUrl = request.getImgUrl();
        this.price = request.getPrice();
    }
}
