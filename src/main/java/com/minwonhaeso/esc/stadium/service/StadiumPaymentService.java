package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.AuthErrorCode.*;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class StadiumPaymentService {
    private final StadiumRepository stadiumRepository;
    private final StadiumReservationItemRepository stadiumReservationItemRepository;
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumItemRepository stadiumItemRepository;
    private final RedissonLockReservingTimeFacade redissonLockReservingTimeFacade;


    public Map<String, String> payment(Member member, Long stadiumId, StadiumPaymentDto.PaymentRequest request) {
        if (!member.getEmail().equals(request.getEmail())) throw new AuthException(EmailNotMatched);
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new StadiumException(StadiumNotFound));
        try {
            redissonLockReservingTimeFacade.lock(stadiumId, request.getDate());
        } catch (StadiumException e) {
            throw e;
        }

        List<ReservingTime> reservingTimes = request.getReservedTimes().stream()
                .map(ReservingTime::findTime)
                .collect(Collectors.toList());
        if (isAlreadyReservedTimes(stadium, request.getDate(), reservingTimes)) {
            throw new StadiumException(AlreadyReservedTime);
        }
        StadiumReservation reservation = StadiumReservation.builder()
                .stadium(stadium)
                .member(member)
                .reservingDate(request.getDate())
                .reservingTimes(reservingTimes)
                .price(request.getTotalPrice())
                .headCount(request.getHeadCount())
                .status(StadiumReservationStatus.RESERVED)
                .paymentType(PaymentType.valueOf(request.getPaymentType()))
                .build();
        stadiumReservationRepository.save(reservation);
        List<StadiumReservationItem> items = new ArrayList<>();
        if (request.getItems().size() > 0) {
            for (int i = 0; i < request.getItems().size(); i++) {
                ItemRequest item = request.getItems().get(i);
                try {
                    StadiumItem stadiumItem = stadiumItemRepository.findById(item.getId())
                            .orElseThrow(() -> new StadiumException(ItemNotFound));

                    items.add(StadiumReservationItem.builder()
                            .item(stadiumItem)
                            .count(item.getCount())
                            .reservation(reservation)
                            .price(item.getCount() * stadiumItem.getPrice())
                            .build());
                } catch (StadiumException e) {
                    log.info("item not found. id:" + item.getId());
                }
            }
            reservation.setItems(items);
            stadiumReservationItemRepository.saveAll(items);
        }
        stadiumReservationRepository.save(reservation);
        redissonLockReservingTimeFacade.unlock(stadiumId, request.getDate());
        Map<String, String> result = new HashMap<>();
        result.put("successMessage", "예약이 완료되었습니다.");
        return result;
    }

    private boolean isAlreadyReservedTimes(
            Stadium stadium, LocalDate date,
            List<ReservingTime> reservingTimes
    ) {
        List<ReservingTime> reservedTimes = new ArrayList<>();
        stadiumReservationRepository
                .findAllByStadiumAndReservingDate(stadium, date)
                .forEach(reservation -> reservedTimes.addAll(reservation.getReservingTimes()));

        for (ReservingTime time : reservingTimes) {
            if (reservedTimes.contains(time)) {
                return true;
            }
        }

        return false;
    }
}
