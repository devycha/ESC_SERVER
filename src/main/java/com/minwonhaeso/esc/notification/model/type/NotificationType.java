package com.minwonhaeso.esc.notification.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    REVIEW("/stadiums/{}/info"),
    RESERVATION("/stadiums/{}/reservations/{}");

    private final String url;
}
