package com.minwonhaeso.esc.notification.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    REVIEW("/stadiums/%d/info"),
    RESERVATION("/stadiums/%d/reservations/%d");

    private final String url;
}
