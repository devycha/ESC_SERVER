package com.minwonhaeso.esc.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class HolidayUtil {
    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public static boolean isHoliday(LocalDate date) {
        // TODO: 법정 공휴일 추가 예정
        return false;
    }
}
