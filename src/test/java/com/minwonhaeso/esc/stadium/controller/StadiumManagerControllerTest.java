package com.minwonhaeso.esc.stadium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.WebSecurityConfig;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.security.auth.jwt.JwtAuthenticationFilter;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumItemStatus;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

        stadium.setImgs(imgs);
        stadium.setRentalStadiumItems(items);
        stadium.setTags(tags);

        pageable = PageRequest.of(0, 20);
        stadiums = new PageImpl<>(List.of(StadiumResponseDto.fromEntity(stadium)), pageable, 0);
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
}