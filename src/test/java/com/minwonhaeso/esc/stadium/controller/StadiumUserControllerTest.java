package com.minwonhaeso.esc.stadium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.WebSecurityConfig;
import com.minwonhaeso.esc.security.auth.jwt.JwtAuthenticationFilter;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.model.dto.StadiumInfoResponseDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
import com.minwonhaeso.esc.stadium.service.StadiumSearchService;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.LatLntInvalid;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = StadiumUserController.class,
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
@WithMockUser(username="admin", roles={ "USER" })
class StadiumUserControllerTest {
    @MockBean
    private StadiumService stadiumService;

    @MockBean
    private StadiumSearchService stadiumSearchService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Stadium stadium;
    private List<StadiumItem> items;
    private List<StadiumImg> imgs;
    private List<StadiumTag> tags;
    private PageRequest pageable;
    private Page<StadiumResponseDto> stadiums;
    private Page<StadiumDocument> stadiumDocuments;

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

        pageable = PageRequest.of(0, 20);
        stadiums = new PageImpl<>(List.of(StadiumResponseDto.fromEntity(stadium)), pageable, 0);
        stadiumDocuments = new PageImpl<>(List.of(StadiumDocument.fromEntity(stadium)), pageable, 0);
    }

    @Test
    @DisplayName("체육관 조회 성공")
    void getAllStadiumsTest_Success() throws Exception {
        // given
        given(stadiumService.getAllStadiums(pageable)).willReturn(stadiums);

        // then
        mockMvc.perform(get("/stadiums"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].stadiumId").value(stadium.getId()))
                .andExpect(jsonPath("$.content[0].name").value(stadium.getName()))
                .andExpect(jsonPath("$.content.size()").value(stadiums.getTotalElements()));
    }

    @Test
    @DisplayName("체육관 상세 조회 실패 : 일치하는 체육관 정보 없음")
    void getStadiumInfoTest_Fail_StadiumNotFound() throws Exception {
        // given
        given(stadiumService.getStadiumInfo(anyLong()))
                .willThrow(new StadiumException(StadiumNotFound));

        // then
        mockMvc.perform(get("/stadiums/3/info"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 상세 조회 성공")
    void getStadiumInfoTest_Success() throws Exception {
        // given
        given(stadiumService.getStadiumInfo(anyLong()))
                .willReturn(StadiumInfoResponseDto.fromEntity(stadium));

        // then
        mockMvc.perform(get("/stadiums/3/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stadium.getId()))
                .andExpect(jsonPath("$.name").value(stadium.getName()));
    }

    @Test
    @DisplayName("가까운 체육관 조회 성공")
    void getAllStadiumsNearLocationTest_Success() throws Exception {
        // given
        given(stadiumSearchService.getAllStadiumsNearLocation(anyDouble(), anyDouble(), any()))
                .willReturn(List.of(StadiumResponseDto.fromEntity(stadium)));

        // then
        mockMvc.perform(get("/stadiums/near-loc?lat=37.5&lnt=127.5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stadiumId").value(stadium.getId()))
                .andExpect(jsonPath("$[0].name").value(stadium.getName()));
    }

    @Test
    @DisplayName("가까운 체육관 조회 실패 : 위도 경도 값 오류")
    void getAllStadiumsNearLocationTest_Fail_LatLntInvalid() throws Exception {
        // given
        given(stadiumSearchService.getAllStadiumsNearLocation(anyDouble(), anyDouble(), any()))
                .willThrow(new StadiumException(LatLntInvalid));

        // then
        mockMvc.perform(get("/stadiums/near-loc?lat=-91&lnt=190"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(LatLntInvalid.getErrorMessage()));
    }

    @Test
    @DisplayName("체육관 검색 성공")
    void searchStadiumTest_Success() throws Exception {
        // given
        given(stadiumSearchService.search(anyString(), any()))
                .willReturn(stadiumDocuments);

        // then
        mockMvc.perform(get("/stadiums/search?searchValue=체육관"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(stadium.getId()))
                .andExpect(jsonPath("$.content[0].name").value(stadium.getName()))
                .andExpect(jsonPath("$.content.size()").value(stadiums.getTotalElements()));
    }
}