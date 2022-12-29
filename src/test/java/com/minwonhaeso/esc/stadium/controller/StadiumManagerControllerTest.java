package com.minwonhaeso.esc.stadium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.WebSecurityConfig;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.security.auth.jwt.JwtAuthenticationFilter;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto.CreateStadiumRequest;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationInfoResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.StadiumReservationUserResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.service.StadiumReservationService;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = StadiumManagerController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                WebSecurityConfig.class,
                                JwtAuthenticationFilter.class
                        }
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser(username="admin", roles={ "MANAGER" })
class StadiumManagerControllerTest {
    @MockBean
    private StadiumService stadiumService;

    @MockBean
    private StadiumReservationService stadiumReservationService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Stadium stadium;
    private StadiumReservation reservation;
    private List<StadiumItem> items;
    private List<StadiumImg> imgs;
    private List<StadiumTag> tags;
    private List<StadiumReservationItem> rentalItems;
    private PageRequest pageable;
    private Page<StadiumResponseDto> stadiums;
    private Page<StadiumReservationUserResponse> reservations;

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
                .role(MemberRole.ROLE_STADIUM)
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
                .items(rentalItems)
                .paymentType(PaymentType.ACCOUNT)
                .createdAt(LocalDateTime.now())
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
        reservation.setItems(rentalItems);

        pageable = PageRequest.of(0, 20);
        stadiums = new PageImpl<>(List.of(StadiumResponseDto.fromEntity(stadium)), pageable, 0);
        reservations = new PageImpl<>(List.of(StadiumReservationUserResponse.fromEntity(reservation)));
    }

    @Test
    @DisplayName("내가 등록한 체육관 조회 성공")
    void getAllRegisteredStadiumsByManagerTest_Success() throws Exception {
        // given
        given(stadiumService.getAllStadiumsByManager(any(), any()))
                .willReturn(stadiums);

        // then
        mockMvc.perform(get("/stadiums/manager").with(user(PrincipalDetail.of(member))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].stadiumId").value(stadium.getId()))
                .andExpect(jsonPath("$.content[0].name").value(stadium.getName()))
                .andExpect(jsonPath("$.content[0].lat").value(stadium.getLat()))
                .andExpect(jsonPath("$.content[0].lnt").value(stadium.getLnt()))
                .andExpect(jsonPath("$.content[0].address").value(stadium.getAddress()))
                .andExpect(jsonPath("$.content[0].starAvg").value(stadium.getStarAvg()))
                .andExpect(jsonPath("$.content[0].weekdayPricePerHalfHour").value(stadium.getWeekdayPricePerHalfHour()))
                .andExpect(jsonPath("$.content[0].holidayPricePerHalfHour").value(stadium.getHolidayPricePerHalfHour()))
                .andExpect(jsonPath("$.content[0].imgUrl").value(stadium.getImgs().get(0).getImgUrl()))
                .andExpect(jsonPath("$.content[0].tags.size()").value(stadium.getTags().size()));
    }

    @Test
    @DisplayName("체육관 누적 사용자 조회 실패 : 일치하는 체육관 정보 없음")
    void getAllReservationUsersTest_Fail_StadiumNotFound() throws Exception {
        // givenx
        given(stadiumReservationService.getAllReservationUsersByManager(any(), anyLong(), any()))
                .willThrow(new StadiumException(StadiumNotFound));

        //then
        mockMvc.perform(get("/stadiums/manager/2")
                .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 누적 사용자 조회 실패 : 일치하는 체육관 정보 없음")
    void getAllReservationUsersTest_Fail_UnAuthorizedAccess() throws Exception {
        // given
        given(stadiumReservationService.getAllReservationUsersByManager(any(), anyLong(), any()))
                .willThrow(new StadiumException(UnAuthorizedAccess));

        //then
        mockMvc.perform(get("/stadiums/manager/2")
                .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value(UnAuthorizedAccess.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 누적 사용자 조회 성공")
    void getAllReservationUsersTest_Success() throws Exception {
        // given
        given(stadiumReservationService.getAllReservationUsersByManager(any(), anyLong(), any()))
                .willReturn(reservations);

        //then
        mockMvc.perform(get("/stadiums/manager/2")
                .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].stadiumId").value(stadium.getId()))
                .andExpect(jsonPath("$.content[0].reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$.content[0].name").value(member.getName()))
                .andExpect(jsonPath("$.content[0].reservingDate").value(reservation.getReservingDate().toString()))
                .andExpect(jsonPath("$.content[0].paymentDate").value(reservation.getCreatedAt().toString()))
                .andExpect(jsonPath("$.content[0].status").value(reservation.getStatus().toString()));
    }

    @Test
    @DisplayName("체육관 예약 상세 정보 조회 실패 : 일치하는 예약 정보 없음")
    void getReservationInfoByManagerTest_Fail_ReservationNotFound() throws Exception {
        // given
        given(stadiumReservationService.getReservationInfoByManager(any(), anyLong(), anyLong()))
                .willThrow(new StadiumException(ReservationNotFound));

        // then
        mockMvc.perform(get("/stadiums/manager/2/reservations/3")
                        .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(ReservationNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 예약 상세 정보 조회 실패 : 일치하는 체육관 정보 없음")
    void getReservationInfoByManagerTest_Fail_StadiumNotFound() throws Exception {
        // given
        given(stadiumReservationService.getReservationInfoByManager(any(), anyLong(), anyLong()))
                .willThrow(new StadiumException(StadiumNotFound));

        // then
        mockMvc.perform(get("/stadiums/manager/2/reservations/3")
                        .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 예약 상세 정보 조회 실패 : 예약 정보와 체육관 정보가 일치하지 않음")
    void getReservationInfoByManagerTest_Fail_StadiumReservationNotMatch() throws Exception {
        // given
        given(stadiumReservationService.getReservationInfoByManager(any(), anyLong(), anyLong()))
                .willThrow(new StadiumException(StadiumReservationNotMatch));

        // then
        mockMvc.perform(get("/stadiums/manager/2/reservations/3")
                        .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumReservationNotMatch.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 예약 상세 정보 조회 실패 : 체육관 매니저 정보와 현재 접속한 사용자 정보가 일치하지 않음(권한 없음)")
    void getReservationInfoByManagerTest_Fail_UnAuthorizedAccess() throws Exception {
        // given
        given(stadiumReservationService.getReservationInfoByManager(any(), anyLong(), anyLong()))
                .willThrow(new StadiumException(UnAuthorizedAccess));

        // then
        mockMvc.perform(get("/stadiums/manager/2/reservations/3")
                        .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value(UnAuthorizedAccess.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 예약 상세 정보 조회 성공")
    void getReservationInfoByManagerTest_Success() throws Exception {
        // given
        given(stadiumReservationService.getReservationInfoByManager(any(), anyLong(), anyLong()))
                .willReturn(ReservationInfoResponse.fromEntity(reservation));

        //then
        mockMvc.perform(get("/stadiums/manager/2/reservations/3")
                        .with(user(PrincipalDetail.of(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$.stadiumId").value(stadium.getId()))
                .andExpect(jsonPath("$.name").value(stadium.getName()))
                .andExpect(jsonPath("$.status").value(reservation.getStatus().toString()))
                .andExpect(jsonPath("$.member.id").value(member.getMemberId()))
                .andExpect(jsonPath("$.member.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.member.email").value(member.getEmail()))
                .andExpect(jsonPath("$.reservingDate").value(reservation.getReservingDate().toString()))
                .andExpect(jsonPath("$.reservingTime").value(reservation.getReservingTimes().stream().map(ReservingTime::getTime).collect(Collectors.toList())))
                .andExpect(jsonPath("$.headCount").value(reservation.getHeadCount()))
                .andExpect(jsonPath("$.price").value(reservation.getPrice()))
                .andExpect(jsonPath("$.paymentType").value(reservation.getPaymentType().toString()))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("체육관 신규 등록 실패 : 매니저로 등록된 사용자가 아님.")
    void createStadiumTest_Fail_NotManager() throws Exception {
        // given
        Member notManagerUser = Member.builder()
                .memberId(1L)
                .email("email")
                .name("Name")
                .nickname("Nickname")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .status(MemberStatus.ING)
                .imgUrl("Image URL")
                .providerId("email")
                .providerType(ProviderType.LOCAL)
                .type(MemberType.USER)
                .role(MemberRole.ROLE_USER)
                .build();

        CreateStadiumRequest request = CreateStadiumRequest.builder()
                .name("Stadium Name")
                .phone("01012345678")
                .address("Stadium Address")
                .detailAddress("Stadium Detail Address")
                .lat(37.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(1000)
                .holidayPricePerHalfHour(2000)
                .openTime("08:00")
                .closeTime("20:00")
                .imgs(new ArrayList<>())
                .tags(new ArrayList<>())
                .rentalItems(new ArrayList<>())
                .build();

        mockMvc.perform(post("/stadiums/register")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType("application/json")
                        .with(user(PrincipalDetail.of(notManagerUser))))
                .andExpect(status().isForbidden());
    }

//    @Test
//    @DisplayName("체육관 신규 등록 성공")
//    void createStadiumTest_Success() throws Exception {
//        // given
//        CreateStadiumRequest request = CreateStadiumRequest.builder()
//                .name("Stadium Name")
//                .phone("01012345678")
//                .address("Stadium Address")
//                .detailAddress("Stadium Detail Address")
//                .lat(37.5)
//                .lnt(127.5)
//                .weekdayPricePerHalfHour(1000)
//                .holidayPricePerHalfHour(2000)
//                .openTime("08:00")
//                .closeTime("20:00")
//                .imgs(new ArrayList<>())
//                .tags(new ArrayList<>())
//                .rentalItems(new ArrayList<>())
//                .build();
//
//        Stadium newStadium = Stadium.fromRequest(request, member);
//        newStadium.setId(2L);
//
//        given(stadiumService.createStadium(request, member))
//                .willReturn(StadiumDto.CreateStadiumResponse.fromEntity(newStadium));
//
//        var TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
//        var httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
//        var csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());
//
//        mockMvc.perform(post("/stadiums/register")
//                        .with(user(PrincipalDetail.of(member)))
//                        .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
//                        .param(csrfToken.getParameterName(), csrfToken.getToken())
//                        .content(objectMapper.writeValueAsString(request))
//                        .contentType("application/json"))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.stadium.memberId").value(member.getMemberId()))
//                .andExpect(jsonPath("$.stadium.name").value(member.getName()))
//                .andExpect(jsonPath("$.stadium.lat").value(request.getLat()))
//                .andExpect(jsonPath("$.stadium.lnt").value(request.getLnt()))
//                .andExpect(jsonPath("$.stadium.address").value(request.getAddress()))
//                .andExpect(jsonPath("$.stadium.detailAddress").value(request.getDetailAddress()))
//                .andExpect(jsonPath("$.stadium.phone").value(request.getPhone()))
//                .andExpect(jsonPath("$.stadium.starAvg").value(0L))
//                .andExpect(jsonPath("$.stadium.weekdayPricePerHalfHour").value(request.getWeekdayPricePerHalfHour()))
//                .andExpect(jsonPath("$.stadium.holidayPricePerHalfHour").value(request.getHolidayPricePerHalfHour()))
//                .andExpect(jsonPath("$.stadium.rentalItems").isEmpty())
//                .andExpect(jsonPath("$.stadium.imgs").isEmpty())
//                .andExpect(jsonPath("$.stadium.tags").isEmpty())
//                .andExpect(jsonPath("$.stadium.openTime").value(request.getOpenTime()))
//                .andExpect(jsonPath("$.stadium.closeTime").value(request.getCloseTime()))
//                .andExpect(jsonPath("$.stadium.isLike").value(false));
//    }
}























