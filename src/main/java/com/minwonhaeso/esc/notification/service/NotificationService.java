package com.minwonhaeso.esc.notification.service;

import com.minwonhaeso.esc.error.exception.NotificationException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto.CheckNotificationResponse;
import com.minwonhaeso.esc.notification.model.entity.Notification;
import com.minwonhaeso.esc.notification.model.type.NotificationType;
import com.minwonhaeso.esc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.List;

import static com.minwonhaeso.esc.error.type.NotificationErrorCode.UnAuthorizedAccess;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Page<NotificationDto.Response> getNotificationByMember(Member member, Pageable pageable) {
        return notificationRepository.findAllByReceiverOrderByCreatedAtDesc(member, pageable)
                .map(NotificationDto.Response::fromEntity);
    }

    public void createNotification(
            NotificationType type,
            Long baseId,
            Long serveId,
            String message,
            Member member
    ) {
        notificationRepository.save(Notification.builder()
                .message(message)
                .url(type == NotificationType.REVIEW ?
                        String.format(type.getUrl(), baseId)
                        : String.format(type.getUrl(), baseId, serveId))
                .isRead(false)
                .type(type)
                .receiver(member)
                .build());
    }

    public void readNotification(Member member, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotificationException(UnAuthorizedAccess)
        );

        notification.readNotification();
        notificationRepository.save(notification);
    }

    // TODO: 크론 주기 환경 변수
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteNotification() {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification not : notifications) {
            if (not.isRead()) {
                notificationRepository.delete(not);
            }
        }
    }

    public CheckNotificationResponse checkUnReadNotification(Member member) {
        Long unReadCounts = notificationRepository.countByReceiverAndIsReadIsFalse(member);
        return CheckNotificationResponse.builder()
                .result(unReadCounts > 0)
                .cnt(unReadCounts)
                .build();
    }
}
