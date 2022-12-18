package com.minwonhaeso.esc.stadium.model.entity;


import com.minwonhaeso.esc.stadium.model.dto.StadiumTagDto;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stadium_tag")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class StadiumTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Column(name = "name")
    private String name;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static StadiumTag fromRequest(StadiumTagDto request, Stadium stadium) {
        return StadiumTag.builder()
                .stadium(stadium)
                .name(request.getTagName())
                .build();
    }

    public void setAll(StadiumTagDto request) {
        this.name = request.getTagName();
    }
}
