package com.minwonhaeso.esc.stadium.service;


import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.repository.StadiumItemRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationItemRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StadiumPaymentServiceTest {
    @InjectMocks
    public StadiumPaymentService stadiumPaymentService;
    @Mock
    public StadiumReservationItemRepository stadiumReservationItemRepository;
    @Mock
    public StadiumRepository stadiumRepository;
    @Mock
    public StadiumReservationRepository stadiumReservationRepository;
    @Mock
    public StadiumItemRepository stadiumItemRepository;
    @Mock
    public RedissonLockReservingTimeFacade redissonLockReservingTimeFacade;

    @Before("")
    public void setup() {
        MockitoAnnotations.initMocks(this);
        stadiumPaymentService = new StadiumPaymentService(stadiumRepository,
                stadiumReservationItemRepository, stadiumReservationRepository, stadiumItemRepository,
                redissonLockReservingTimeFacade);
    }

    @DisplayName("결제 성공")
    @Test
    void payment_Success() {
        Long stadiumId = 1L;
        List<StadiumItem> likeList = new ArrayList<>();
        StadiumItem item = StadiumItem.builder()
                .name("축구공")
                .price(30000)
                .build();
        likeList.add(item);
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();
        Stadium stadium = Stadium.builder()
                .id(stadiumId)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.RT19)
                .closeTime(ReservingTime.RT37)
                .rentalStadiumItems(likeList)
                .build();
        List<String> reservedTimes = new ArrayList<>();
        List<ItemRequest> items = new ArrayList<>();
        items.add(new ItemRequest(1L, 2));
        reservedTimes.add("09:00");
        PaymentRequest request = PaymentRequest.builder()
                .date(LocalDate.now().plusDays(1))
                .reservedTimes(reservedTimes)
                .headCount(2)
                .items(items)
                .totalPrice(10000)
                .email(member.getEmail())
                .paymentType("CARD")
                .build();
        //given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));
        given(stadiumItemRepository.findById(anyLong()))
                .willReturn(Optional.of(item));
        //when
        Map<String,String> map =  stadiumPaymentService.payment(member,stadiumId,request);
        //then
        assertNotNull(map.get("successMessage"));
    }
}