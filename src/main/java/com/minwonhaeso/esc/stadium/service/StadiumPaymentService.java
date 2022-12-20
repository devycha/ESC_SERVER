package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.PaymentConfirmResponse;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumPayment;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.repository.StadiumPaymentRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.AuthErrorCode.*;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static com.minwonhaeso.esc.member.model.type.PaymentExpirationEnums.PAYMENT_ACCESS_TIME;
import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;

@RequiredArgsConstructor
@Service
public class StadiumPaymentService {
    private final StadiumRepository stadiumRepository;
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumPaymentRepository stadiumPaymentRepository;

    public PaymentConfirmResponse paymentConfirm(PaymentConfirmRequest request, Member member, Long stadiumId) {
        int price = request.getReservedTimes().size() * request.getPricePerHalfHour();
        StadiumPayment redis = StadiumPayment.builder()
                .id(member.getEmail())
                .stadiumId(stadiumId)
                .date(request.getDate())
                .expireDt(System.currentTimeMillis() + PAYMENT_ACCESS_TIME.getValue())
                .price(price)
                .headCount(request.getHeadCount())
                .items(request.getItems())
                .reservedTimes(request.getReservedTimes())
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
        stadiumReservationRepository.save(reservation);
        stadiumPaymentRepository.delete(stadiumPayment);
        Map<String,String> result = new HashMap<>();
        result.put("successMessage","예약이 완료되었습니다.");
        return result;
    }
}
