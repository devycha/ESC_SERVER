package com.minwonhaeso.esc.notification.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.model.dto.NotificationDto;
import com.minwonhaeso.esc.notification.service.NotificationService;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<?> checkUnReadNotification(
            @AuthenticationPrincipal PrincipalDetail principalDetail
    ) {
        Member member = principalDetail.getMember();
        NotificationDto.CheckNotificationResponse result =
                notificationService.checkUnReadNotification(member);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping()
    public ResponseEntity<?> getAllNotifications(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            Pageable pageable
    ) {
        Member member = principalDetail.getMember();
        Page<NotificationDto.Response> notifications =
                notificationService.getNotificationByMember(member, pageable);
        return ResponseEntity.ok().body(notifications);
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<?> readNotification(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long notificationId
    ) {
        Member member = principalDetail.getMember();
        notificationService.readNotification(member, notificationId);
        return ResponseEntity.ok().build();
    }
}
