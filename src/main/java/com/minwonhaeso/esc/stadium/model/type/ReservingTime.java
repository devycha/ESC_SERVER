package com.minwonhaeso.esc.stadium.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ReservingTime {
    RT1("00:00"),
    RT2("00:30"),
    RT3("01:00"),
    RT4("01:30"),
    RT5("02:00"),
    RT6("02:30"),
    RT7("03:00"),
    RT8("03:30"),
    RT9("04:00"),
    RT10("04:30"),
    RT11("05:00"),
    RT12("05:30"),
    RT13("06:00"),
    RT14("06:30"),
    RT15("07:00"),
    RT16("07:30"),
    RT17("08:00"),
    RT18("08:30"),
    RT19("09:00"),
    RT20("09:30"),
    RT21("10:00"),
    RT22("10:30"),
    RT23("11:00"),
    RT24("11:30"),
    RT25("12:00"),
    RT26("12:30"),
    RT27("13:00"),
    RT28("13:30"),
    RT29("14:00"),
    RT30("14:30"),
    RT31("15:00"),
    RT32("15:30"),
    RT33("16:00"),
    RT34("16:30"),
    RT35("17:00"),
    RT36("17:30"),
    RT37("18:00"),
    RT38("18:30"),
    RT39("19:00"),
    RT40("19:30"),
    RT41("20:00"),
    RT42("20:30"),
    RT43("21:00"),
    RT44("21:30"),
    RT45("22:00"),
    RT46("22:30"),
    RT47("23:00"),
    RT48("23:30"),
    RT49("24:00");

    private final String time;
    public static ReservingTime findTime(String findTime) {
        return Arrays.stream(ReservingTime.values())
                .filter(reservingTime -> reservingTime.getTime().equals(findTime))
                .collect(Collectors.toList())
                .get(0);
    }
}
