package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.member.service.CustomerMemberDetailsService;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumLikeRequestDto;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.repository.StadiumLikeRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StadiumLikeServiceTest {
    @InjectMocks
    public StadiumLikeService stadiumLikeService;
    @Mock
    public StadiumLikeRepository stadiumLikeRepository;
    @Mock
    public StadiumRepository stadiumRepository;
    @Before("")
    public void setup(){
        MockitoAnnotations.initMocks(this);
        stadiumLikeService = new StadiumLikeService(stadiumLikeRepository,stadiumRepository);
    }

    @Test
    @DisplayName("찜하기 성공")
    void likesSuccess (){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();
        Stadium stadium = Stadium.builder()
                .id(1L)
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
                .build();
        //given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));
        //when
        StadiumLikeRequestDto dto =  stadiumLikeService.likes(1L,member);
        //then
        assertTrue(dto.isResult());
    }
    @Test
    void likesFail_StadiumNotFound(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();
        Stadium stadium = Stadium.builder()
                .id(1L)
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
                .build();
        //given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        StadiumException exception =  assertThrows(
                StadiumException.class,
                ()-> stadiumLikeService.likes(2L,member));
        //then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.StadiumNotFound);
    }


}