package com.minwonhaeso.esc.notification.service;

import com.minwonhaeso.esc.error.exception.NotificationException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto.CheckNotificationResponse;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto.ReadNotificationResponse;
import com.minwonhaeso.esc.notification.model.entity.Notification;
import com.minwonhaeso.esc.notification.model.type.NotificationType;
import com.minwonhaeso.esc.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.minwonhaeso.esc.error.type.NotificationErrorCode.UnAuthorizedAccess;
import static com.minwonhaeso.esc.notification.model.type.NotificationType.RESERVATION;
import static com.minwonhaeso.esc.notification.model.type.NotificationType.REVIEW;

@RequiredArgsConstructor
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> getAllReadNotifications(Member member, Pageable pageable) {
        return notificationRepository
                .findAllByReceiverAndIsReadIsTrueOrderByCreatedAtDesc(member, pageable)
                .map(NotificationDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> getAllUnreadNotifications(Member member, Pageable pageable) {
        return notificationRepository
                .findAllByReceiverAndIsReadIsFalseOrderByCreatedAtDesc(member, pageable)
                .map(NotificationDto.Response::fromEntity);
    }

    public void createNotification(
            NotificationType type,
            Long baseId,
            String message,
            Member member
    ) {
        notificationRepository.save(Notification.builder()
                .message(message)
                .url(type == REVIEW ?
                        String.format(type.getUrl(), baseId)
                        : RESERVATION.getUrl())
                .isRead(false)
                .type(type)
                .receiver(member)
                .build());
    }

    public ReadNotificationResponse readNotification(Member member, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotificationException(UnAuthorizedAccess)
        );

        notification.readNotification();
        notificationRepository.save(notification);

        return ReadNotificationResponse.builder().result(true).build();
    }

    @Scheduled(cron = "${scheduler.update.notification}")
    public void deleteNotification() {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification not : notifications) {
            if (not.isRead()) {
                notificationRepository.delete(not);
            }
        }
    }

    @Transactional(readOnly = true)
    public CheckNotificationResponse checkUnReadNotification(Member member) {
        Long unReadCounts = notificationRepository.countByReceiverAndIsReadIsFalse(member);
        return CheckNotificationResponse.builder()
                .result(unReadCounts > 0)
                .cnt(unReadCounts)
                .build();
    }
}
