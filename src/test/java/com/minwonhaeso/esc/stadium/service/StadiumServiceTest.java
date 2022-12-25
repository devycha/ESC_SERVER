package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto.CreateStadiumResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumInfoResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumImg;
import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import com.minwonhaeso.esc.stadium.model.entity.StadiumTag;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
import com.minwonhaeso.esc.stadium.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.UnAuthorizedAccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {
    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private StadiumImgRepository stadiumImgRepository;

    @Mock
    private StadiumTagRepository stadiumTagRepository;

    @Mock
    private StadiumItemRepository stadiumItemRepository;

    @Mock
    private StadiumSearchRepository stadiumSearchRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private StadiumService stadiumService;

    private Member member;
    private Stadium stadium;
    private List<StadiumItem> items;
    private List<StadiumImg> imgs;
    private List<StadiumTag> tags;

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

        stadium.setImgs(imgs);
        stadium.setRentalStadiumItems(items);
        stadium.setTags(tags);
    }

    @Test
    @DisplayName("체육관 상세 정보 조회 성공")
    void getStadiumInfoTest_Success() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        StadiumInfoResponseDto stadiumInfo = stadiumService.getStadiumInfo(1L);

        // then
        assertEquals(stadium.getId(), stadiumInfo.getId());
    }

    @Test
    @DisplayName("체육관 상세 정보 조회 실패 : 일치하는 체육관 없음")
    void getStadiumInfoTest_Fail_StadiumNotFound() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> stadiumService.getStadiumInfo(123L));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("체육관 생성 성공")
    void createStadiumTest_Success() {
        // given
        StadiumDto.CreateStadiumRequest request = StadiumDto.CreateStadiumRequest.builder()
                .name("Stadium Name")
                .phone("01012345678")
                .address("Address")
                .detailAddress("Detail Address")
                .lat(37.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(20000)
                .holidayPricePerHalfHour(30000)
                .openTime("09:00")
                .closeTime("18:00")
                .build();

        // when
        CreateStadiumResponse response = stadiumService.createStadium(request, member);

        // then
        assertEquals(request.getName(), response.getStadium().getName());
        assertEquals(request.getPhone(), response.getStadium().getPhone());
        assertEquals(request.getLat(), response.getStadium().getLat());
        assertEquals(request.getLnt(), response.getStadium().getLnt());
        assertEquals(request.getAddress(), response.getStadium().getAddress());
        assertEquals(request.getDetailAddress(), response.getStadium().getDetailAddress());
        assertEquals(request.getWeekdayPricePerHalfHour(), response.getStadium().getWeekdayPricePerHalfHour());
        assertEquals(request.getHolidayPricePerHalfHour(), response.getStadium().getHolidayPricePerHalfHour());
        assertEquals(request.getOpenTime(), response.getStadium().getOpenTime());
        assertEquals(request.getCloseTime(), response.getStadium().getCloseTime());
    }

    @Test
    @DisplayName("체육관 삭제 실패 : 일치하는 체육관 정보 없음")
    void deleteStadiumTest_Fail_StadiumNotFound() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> stadiumService.deleteStadium(member, 1L));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("체육관 삭제 실패 : 권한 없음")
    void deleteStadiumTest_Fail_UnAuthorized() {
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

        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> stadiumService.deleteStadium(anotherMember, 1L));

        // then
        assertEquals(UnAuthorizedAccess.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("체육관 삭제 성공")
    void deleteStadiumTest_Success() {
        // given
        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        stadiumService.deleteStadium(member, 1L);

        // then
        assertEquals(member.getMemberId(), stadium.getMember().getMemberId());
    }

    @Test
    @DisplayName("체육관 정보 수정 실패 : 일치하는 체육관 정보 없음")
    void updateStadiumTest_Fail_StadiumNotFound() {
        // given
        StadiumDto.UpdateStadiumRequest request = StadiumDto.UpdateStadiumRequest.builder()
                .name("Updated Name")
                .phone("01087654321")
                .lat(38.5)
                .lnt(130.5)
                .address("Updated Address")
                .weekdayPricePerHalfHour(25000)
                .holidayPricePerHalfHour(35000)
                .openTime("10:00")
                .closeTime("19:00")
                .build();

        given(stadiumRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> stadiumService.updateStadiumInfo(member, 1L, request));

        // then
        assertEquals(StadiumNotFound.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("체육관 정보 수정 실패 : 권한 없음")
    void updateStadiumTest_Fail_UnAuthorized() {
        // given
        StadiumDto.UpdateStadiumRequest request = StadiumDto.UpdateStadiumRequest.builder()
                .name("Updated Name")
                .phone("01087654321")
                .lat(38.5)
                .lnt(130.5)
                .address("Updated Address")
                .weekdayPricePerHalfHour(25000)
                .holidayPricePerHalfHour(35000)
                .openTime("10:00")
                .closeTime("19:00")
                .build();

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

        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // when
        Exception exception = assertThrows(StadiumException.class,
                () -> stadiumService.updateStadiumInfo(anotherMember, 1L, request));

        // then
        assertEquals(UnAuthorizedAccess.getErrorMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("체육관 정보 수정 성공")
    void updateStadiumTest_Success() {
        // given
        StadiumDto.UpdateStadiumRequest request = StadiumDto.UpdateStadiumRequest.builder()
                .name("Updated Name")
                .phone("01087654321")
                .lat(38.5)
                .lnt(130.5)
                .address("Updated Address")
                .detailAddress("Updated Detail Address")
                .weekdayPricePerHalfHour(25000)
                .holidayPricePerHalfHour(35000)
                .openTime("10:00")
                .closeTime("19:00")
                .build();

        given(stadiumRepository.findById(anyLong())).willReturn(Optional.of(stadium));

        // then
        StadiumInfoResponseDto response = stadiumService.updateStadiumInfo(member, 1L, request);

        // then
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getPhone(), response.getPhone());
        assertEquals(request.getLat(), response.getLat());
        assertEquals(request.getLnt(), response.getLnt());
        assertEquals(request.getAddress(), response.getAddress());
        assertEquals(request.getDetailAddress(), response.getDetailAddress());
        assertEquals(request.getWeekdayPricePerHalfHour(), response.getWeekdayPricePerHalfHour());
        assertEquals(request.getHolidayPricePerHalfHour(), response.getHolidayPricePerHalfHour());
        assertEquals(request.getOpenTime(), response.getOpenTime());
        assertEquals(request.getCloseTime(), response.getCloseTime());
    }
}