package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.PaymentConfirmResponse;
import com.minwonhaeso.esc.stadium.model.entity.StadiumPayment;
import com.minwonhaeso.esc.stadium.repository.StadiumPaymentRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.minwonhaeso.esc.member.model.type.PaymentExpirationEnums.PAYMENT_ACCESS_TIME;
import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;

@RequiredArgsConstructor
@Service
public class StadiumPaymentService {
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumPaymentRepository stadiumPaymentRepository;

    public PaymentConfirmResponse paymentConfirm(PaymentConfirmRequest request, Member member){
        Long price = (long) request.getReservedTimes().size() * request.getPricePerHalfHour();
        StadiumPayment redis = StadiumPayment.builder()
                .id(member.getEmail())
                .date(request.getDate())
                .expireDt(System.currentTimeMillis() + PAYMENT_ACCESS_TIME.getValue())
                .price(price)
                .headCount(request.getHeadCount())
                .items(request.getItems())
                .reservedTimes(request.getReservedTimes())
                .build();
        stadiumPaymentRepository.save(redis);
        return PaymentConfirmResponse.builder()
                .stadiumId(request.getStadiumId())
                .name(member.getName())
                .date(request.getDate())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .headCount(request.getHeadCount())
                .price(price)
                .build();
    }
}
