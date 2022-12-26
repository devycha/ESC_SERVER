package com.minwonhaeso.esc.notification.model.dto;

import com.minwonhaeso.esc.notification.model.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class NotificationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckNotificationResponse {
        private Long cnt;
        private boolean result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String url;
        private String message;
        private boolean isRead;
        private LocalDateTime createdAt;

        public static Response fromEntity(Notification notification) {
            return Response.builder()
                    .id(notification.getId())
                    .url(notification.getUrl())
                    .message(notification.getMessage())
                    .isRead(notification.isRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadNotificationResponse {
        private boolean result;
    }
}
