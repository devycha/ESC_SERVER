package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.PaymentConfirmResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
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
import static com.minwonhaeso.esc.member.model.type.PaymentExpirationEnums.PAYMENT_ACCESS_TIME;
import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class StadiumPaymentService {
    private final StadiumRepository stadiumRepository;
    private final StadiumReservationItemRepository stadiumReservationItemRepository;
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumPaymentRepository stadiumPaymentRepository;
    private final StadiumItemRepository stadiumItemRepository;
    private final RedissonLockReservingTimeFacade redissonLockReservingTimeFacade;

    public PaymentConfirmResponse paymentConfirm(PaymentConfirmRequest request, Member member, Long stadiumId) {
        int price = request.getReservedTimes().size() * request.getPricePerHalfHour();
        StadiumPayment redis = StadiumPayment.builder()
                .id(member.getEmail())
                .stadiumId(stadiumId)
                .date(request.getDate())
                .expireDt(PAYMENT_ACCESS_TIME.getValue())
                .price(price)
                .headCount(request.getHeadCount())
                .items(request.getItems())
                .reservedTimes(request.getReservedTimes().stream()
                        .map(ReservingTime::findTime)
                        .collect(Collectors.toList()))
                .build();
        stadiumPaymentRepository.save(redis);
        return PaymentConfirmResponse.builder()
                .stadiumId(stadiumId)
                .name(member.getName())
                .date(request.getDate())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .headCount(request.getHeadCount())
                .price(price)
                .build();
    }

    public Map<String, String> payment(Member member, Long stadiumId, StadiumPaymentDto.PaymentRequest request) {
        //예약자 이메일과 접속한 사용자 이메일이 맞는지 확인
        if (!member.getEmail().equals(request.getEmail())) throw new AuthException(EmailNotMatched);
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new StadiumException(StadiumNotFound));
        //Redis 에서  결제 정보 불러오기
        StadiumPayment stadiumPayment = stadiumPaymentRepository.findById(member.getEmail()).orElseThrow(() -> new AuthException(EmailNotMatched));
        //Redis 결제 정보에 들어있는 stadiumId와 현재 구매하고자 하는 경기장의 Id값 비교
        if (!Objects.equals(stadiumPayment.getStadiumId(), stadiumId)) throw new StadiumException(StadiumNotFound);
        // 락 시작
        try {
            redissonLockReservingTimeFacade.lock(stadiumId, stadiumPayment.getDate());
        } catch (StadiumException e) {
            throw e;
        }
        // 이미 예약된 시간인지 확인
        if (isAlreadyReservedTimes(stadium, stadiumPayment.getDate(), stadiumPayment.getReservedTimes())) {
            throw new StadiumException(AlreadyReservedTime);
        }
        //예약 생성
        StadiumReservation reservation = StadiumReservation.builder()
                .stadium(stadium)
                .member(member)
                .reservingDate(stadiumPayment.getDate())
                .reservingTimes(stadiumPayment.getReservedTimes().stream()
                        .map(ReservingTime::valueOf)
                        .collect(Collectors.toList()))
                .price(stadiumPayment.getPrice())
                .headCount(stadiumPayment.getHeadCount())
                .status(StadiumReservationStatus.RESERVED)
                .paymentType(PaymentType.valueOf(request.getPaymentType()))
                .build();
        // Create Item Reservation
        List<StadiumReservationItem> items = new ArrayList<>();
        if (stadiumPayment.getItems().size() > 0) {
            stadiumPayment.getItems().forEach(item -> {
                try {
                    StadiumItem stadiumItem = stadiumItemRepository.findById(item.getItemId())
                            .orElseThrow(() -> new StadiumException(ItemNotFound));

                    items.add(StadiumReservationItem.builder()
                            .item(stadiumItem)
                            .count(item.getCount())
                            .reservation(reservation)
                            .price(item.getCount() * stadiumItem.getPrice())
                            .build());
                } catch (StadiumException e) {
                    log.info("item not found. id:" + item.getItemId());
                }
            });
            reservation.getItems().addAll(items);
            stadiumReservationItemRepository.saveAll(items);
        }
        stadiumReservationRepository.save(reservation);
        stadiumPaymentRepository.delete(stadiumPayment);
        redissonLockReservingTimeFacade.unlock(stadiumId, stadiumPayment.getDate());

        Map<String, String> result = new HashMap<>();
        result.put("successMessage", "예약이 완료되었습니다.");
        return result;
    }

    private boolean isAlreadyReservedTimes(
            Stadium stadium, LocalDate date,
            List<String> reservingTimes
    ) {
        List<ReservingTime> reservedTimes = new ArrayList<>();
        stadiumReservationRepository
                .findAllByStadiumAndReservingDate(stadium, date)
                .forEach(reservation -> reservedTimes.addAll(reservation.getReservingTimes()));

        for (String time : reservingTimes) {
            if (reservedTimes.contains(ReservingTime.valueOf(time))) {
                return true;
            }
        }

        return false;
    }
}
