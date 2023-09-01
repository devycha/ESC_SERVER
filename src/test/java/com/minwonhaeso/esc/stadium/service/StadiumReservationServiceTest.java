package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationInfoResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationStadiumInfoResponse;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StadiumReservationServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private StadiumItemRepository stadiumItemRepository;

    @Mock
    private StadiumReservationRepository stadiumReservationRepository;

    @Mock
    private StadiumReservationItemRepository stadiumReservationItemRepository;

    @Mock
    private StadiumReservationCancelRepository stadiumReservationCancelRepository;

    @Mock
    private RedissonLockReservingTimeFacade redissonLockReservingTimeFacade;

    @InjectMocks
    private StadiumReservationService service;

    private Member member;
    private Stadium stadium;
    private List<StadiumItem> items;
    private List<StadiumImg> imgs;
    private List<StadiumTag> tags;
    private StadiumReservation reservation;
    private List<StadiumReservationItem> rentalItems;

    @BeforeEach
    void beforeEach() {
        member = Member.builder()
                .memberId(1L)
                .email("email")
                .name("Name")
                .nickname("Nickname")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .status(MemberStatus.ING)
                .imgUrl("Image URL")
                .providerId("email")
                .providerType(ProviderType.LOCAL)
                .type(MemberType.MANAGER)
                .build();

        stadium = Stadium.builder()
                .id(1L)
                .name("Some Stadium")
                .phone("01012345678")
                .lat(37.5)
                .lnt(127.5)
                .address("Some Address")
                .detailAddress("Detail Address")
                .weekdayPricePerHalfHour(30000)
                .holidayPricePerHalfHour(40000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .starAvg(3.5)
                .member(member)
                .build();

        items = List.of(StadiumItem.builder()
                .id(1L)
                .name("item")
                .imgId("Stadium Item Image ID")
                .imgUrl("Stadium Item Image URL")
                .price(10000)
                .stadium(stadium)
                .status(StadiumItemStatus.AVAILABLE)
                .build());

        imgs = List.of(StadiumImg.builder()
                .id(1L)
                .stadium(stadium)
                .imgId("Stadium Image ID")
                .imgUrl("Stadium Image URL")
                .build());

        tags = List.of(StadiumTag.builder()
                .id(1L)
                .stadium(stadium)
                .name("Stadium Tag ID")
                .build());

        List<ReservingTime> reservingTimes = new ArrayList<>(
                List.of(ReservingTime.RT20, ReservingTime.RT21)
        );
        reservation = StadiumReservation.builder()
                .id(1L)
                .stadium(stadium)
                .member(member)
                .reservingDate(LocalDate.now())
                .reservingTimes(reservingTimes)
                .price(10000)
                .headCount(3)
                .status(StadiumReservationStatus.RESERVED)
                .paymentType(PaymentType.ACCOUNT)
                .build();

        rentalItems = List.of(StadiumReservationItem.builder()
                .id(1L)
                .reservation(reservation)
                .item(items.get(0))
                .price(1000)
                .count(3)
                .build());

        stadium.setImgs(imgs);
        stadium.setRentalStadiumItems(items);
        stadium.setTags(tags);
    }

    @Test
    @DisplayName("예약 체육관 정보 조회 실패 : 일치하는 체육관 정보 없음")
    void getStadiumReservationInfoTest_Fail_StadiumNotFound() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.getStadiumReservationInfo(1L, LocalDate.now()));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 체육관 정보 조회 성공")
    void getStadiumReservationInfoTest_Success() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));
        given(stadiumReservationRepository.findAllByStadiumAndReservingDate(any(), any()))
                .willReturn(List.of(reservation));

        // when
        ReservationStadiumInfoResponse response =
                service.getStadiumReservationInfo(1L, LocalDate.now());

        // then
        assertEquals(stadium.getId(), response.getStadium().getId());
    }

    @Test
    @DisplayName("예약 상세 내역 조회 실패 : 일치하는 예약 정보 없음")
    void getReservationInfoTest_Fail_ReservationNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.getReservationInfo(member, 1L, 1L));

        // then
        assertEquals(ReservationNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 상세 내역 조회 실패 : 일치하는 체육관 정보 없음")
    void getReservationInfoTest_Fail_StadiumNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.getReservationInfo(member, 1L, 1L));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 상세 내역 조회 실패 : 예약 정보와 체육관의 정보가 옳바르지 않음")
    void getReservationInfoTest_Fail_StadiumReservationNotMatch() {
        // given
        Long wrongStadiumId = 2L;
        Stadium anotherStadium = Stadium.builder()
                .id(wrongStadiumId)
                .name("Another Stadium")
                .phone("01087654321")
                .lat(38.5)
                .lnt(128.5)
                .address("Another Address")
                .detailAddress("Another Detail Address")
                .weekdayPricePerHalfHour(20000)
                .holidayPricePerHalfHour(30000)
                .openTime(ReservingTime.findTime("10:00"))
                .closeTime(ReservingTime.findTime("19:00"))
                .starAvg(3.5)
                .member(member)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(anotherStadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.getReservationInfo(member, wrongStadiumId, 1L));

        // then
        assertEquals(StadiumReservationNotMatch.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 상세 내역 조회 실패 : 예약자 정보와 현재 접속자의 정보가 일치하지 않음(권한 없음)")
    void getReservationInfoTest_Fail_UnAuthorized() {
        // given
        Member anotherMember = Member.builder()
                .memberId(2L)
                .email("another email")
                .name("another name")
                .nickname("another nickname")
                .password(BCrypt.hashpw("another password", BCrypt.gensalt()))
                .status(MemberStatus.ING)
                .imgUrl("Another Image URL")
                .providerId("another email")
                .providerType(ProviderType.LOCAL)
                .type(MemberType.MANAGER)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.getReservationInfo(anotherMember, 1L, 1L));

        // then
        assertEquals(UnAuthorizedAccess.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 상세 내역 조회 성공")
    void getReservationInfoTest_Success() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        ReservationInfoResponse response = service.getReservationInfo(member, 1L, 1L);

        // then
        assertEquals(reservation.getId(), response.getReservationId());
        assertEquals(reservation.getStadium().getId(), response.getStadiumId());
    }

    @Test
    @DisplayName("예약 삭제 실패 : 일치하는 예약 정보 없음")
    void deleteReservationTest_Fail_ReservationNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.deleteReservation(member, 1L, 1L));

        // then
        assertEquals(ReservationNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 실패 : 일치하는 체육관 정보 없음")
    void deleteReservationTest_Fail_StadiumNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.deleteReservation(member, 1L, 1L));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 실패 : 예약 정보와 체육관 정보가 옳바르지 않음")
    void deleteReservationTest_Fail_StadiumReservationNotMatch() {
        // given
        Long wrongStadiumId = 2L;
        Stadium anotherStadium = Stadium.builder()
                .id(wrongStadiumId)
                .name("Another Stadium")
                .phone("01087654321")
                .lat(38.5)
                .lnt(128.5)
                .address("Another Address")
                .detailAddress("Another Detail Address")
                .weekdayPricePerHalfHour(20000)
                .holidayPricePerHalfHour(30000)
                .openTime(ReservingTime.findTime("10:00"))
                .closeTime(ReservingTime.findTime("19:00"))
                .starAvg(3.5)
                .member(member)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(anotherStadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.deleteReservation(member, wrongStadiumId, 1L));

        // then
        assertEquals(StadiumReservationNotMatch.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 실패 : 예약자 정보와 현재 접속한 사용자 정보가 일치하지 않음(권한 없음)")
    void deleteReservationTest_Fail_UnAuthorized() {
        // given
        Member anotherMember = Member.builder()
                .memberId(2L)
                .email("another email")
                .name("another name")
                .nickname("another nickname")
                .password(BCrypt.hashpw("another password", BCrypt.gensalt()))
                .status(MemberStatus.ING)
                .imgUrl("Another Image URL")
                .providerId("another email")
                .providerType(ProviderType.LOCAL)
                .type(MemberType.MANAGER)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.deleteReservation(anotherMember, 1L, 1L));

        // then
        assertEquals(UnAuthorizedAccess.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void deleteReservationTest_Success() {

    }

    @Test
    @DisplayName("예약 사용 완료 실패 : 일치하는 예약 정보 없음")
    void executeReservationTest_Fail_ReservationNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.executeReservation(member, 1L, 1L));

        // then
        assertEquals(ReservationNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 사용 완료 실패 : 일치하는 체육관 정보 없음")
    void executeReservation_Fail_StadiumNotFound() {
        // given
        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.executeReservation(member, 1L, 1L));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 사용 완료 실패 : 예약 정보와 체육관 정보가 옳바르지 않음")
    void executeReservation_Fail_StadiumReservationNotMatch() {
        // given
        Long wrongStadiumId = 2L;
        Stadium anotherStadium = Stadium.builder()
                .id(wrongStadiumId)
                .name("Another Stadium")
                .phone("01087654321")
                .lat(38.5)
                .lnt(128.5)
                .address("Another Address")
                .detailAddress("Another Detail Address")
                .weekdayPricePerHalfHour(20000)
                .holidayPricePerHalfHour(30000)
                .openTime(ReservingTime.findTime("10:00"))
                .closeTime(ReservingTime.findTime("19:00"))
                .starAvg(3.5)
                .member(member)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(anotherStadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.executeReservation(member, wrongStadiumId, 1L));

        // then
        assertEquals(StadiumReservationNotMatch.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 사용 완료 실패 : 예약자 정보와 현재 접속한 사용자 정보가 일치하지 않음(권한 없음)")
    void executeReservation_Fail_UnAuthorized() {
        // given
        Member anotherMember = Member.builder()
                .memberId(2L)
                .email("another email")
                .name("another name")
                .nickname("another nickname")
                .password(BCrypt.hashpw("another password", BCrypt.gensalt()))
                .status(MemberStatus.ING)
                .imgUrl("Another Image URL")
                .providerId("another email")
                .providerType(ProviderType.LOCAL)
                .type(MemberType.MANAGER)
                .build();

        given(stadiumReservationRepository.findById(anyLong())).willReturn(Optional.of(reservation));
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> service.executeReservation(anotherMember, 1L, 1L));

        // then
        assertEquals(UnAuthorizedAccess.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("예약 사용 완료 성공")
    void executeReservation_Success() {

    }
}