package com.minwonhaeso.esc.notification.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    REVIEW("/stadium/detail/%d"),
    RESERVATION("/me/rental");

    private final String url;
}
