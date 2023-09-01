package com.minwonhaeso.esc.notification.repository;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByReceiverAndIsReadIsTrueOrderByCreatedAtDesc(Member receiver, Pageable pageable);
    Page<Notification> findAllByReceiverAndIsReadIsFalseOrderByCreatedAtDesc(Member receiver, Pageable pageable);
    Long countByReceiverAndIsReadIsFalse(Member receiver);
}
