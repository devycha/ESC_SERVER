package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.CreateReservationRequest;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ItemResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.PriceResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationInfoResponse;
import com.minwonhaeso.esc.stadium.model.entity.*;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static com.minwonhaeso.esc.util.HolidayUtil.isHoliday;
import static com.minwonhaeso.esc.util.HolidayUtil.isWeekend;

@Slf4j
@RequiredArgsConstructor
@Service
public class StadiumReservationService {
    private final StadiumRepository stadiumRepository;
    private final StadiumItemRepository stadiumItemRepository;
    private final StadiumReservationRepository stadiumReservationRepository;
    private final StadiumReservationItemRepository stadiumReservationItemRepository;
    private final StadiumReservationCancelRepository stadiumReservationCancelRepository;
    private final RedissonLockReservingTimeFacade redissonLockReservingTimeFacade;

    @Transactional(readOnly = true)
    public Page<StadiumReservationDto.Response> getAllReservationsByMember(
            Member member, Pageable pageable) {
        return stadiumReservationRepository
                .findAllByMemberAndStatusAndReservingDateAfter(
                        member,
                        StadiumReservationStatus.RESERVED,
                        LocalDate.now(),
                        pageable).map(StadiumReservationDto.Response::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getStadiumReservationInfo(
            Long stadiumId, LocalDate date) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        // 해당 스타디움에서 빌릴 수 있는 아이템 정보들
        List<ItemResponse> items =
                stadiumItemRepository.findAllByStadium(stadium).stream()
                        .map(ItemResponse::fromEntity)
                        .collect(Collectors.toList());

        // 해당 스타디움의 해당 날짜에 이미 예약된 시간들
        List<String> reservedTimes = new ArrayList<>();
        stadiumReservationRepository
                .findAllByStadiumAndReservingDate(stadium, date)
                .forEach(reservation -> {
                    reservation.getReservingTimes().forEach(
                            reservingTime -> {
                                reservedTimes.add(reservingTime.getTime());
                            }
                    );
                });

        return ReservationInfoResponse.builder()
                .openTime(stadium.getOpenTime())
                .closeTime(stadium.getCloseTime())
                .stadiumId(stadium.getId())
                .stadiumName(stadium.getName())
                .date(date)
                .pricePerHalfHour(isWeekend(date) || isHoliday(date)
                        ? stadium.getHolidayPricePerHalfHour()
                        : stadium.getWeekdayPricePerHalfHour())
                .items(items)
                .reservedTimes(reservedTimes)
                .build();
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getReservationInfo(
            Member member,
            Long stadiumId,
            Long reservationId
    ) {
        StadiumReservation reservation = stadiumReservationRepository
                .findById(reservationId).orElseThrow(
                        () -> new StadiumException(ReservationNotFound)
                );

        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (!reservation.getStadium().getId().equals(stadiumId)) {
            throw new StadiumException(StadiumReservationNotMatch);
        }

        if (!reservation.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        List<ItemResponse> items =
                stadiumReservationItemRepository.findAllByReservation(reservation)
                        .stream()
                        .map(ItemResponse::fromReservationItem)
                        .collect(Collectors.toList());

        return ReservationInfoResponse.builder()
                .openTime(stadium.getOpenTime())
                .closeTime(stadium.getCloseTime())
                .stadiumId(stadiumId)
                .stadiumName(stadium.getName())
                .reservedTimes(reservation.getReservingTimes().stream()
                        .map(ReservingTime::getTime)
                        .collect(Collectors.toList()))
                .pricePerHalfHour(reservation.getPrice())
                .date(reservation.getReservingDate())
                .items(items)
                .build();
    }

    public void deleteReservation(Member member, Long stadiumId, Long reservationId) {
        StadiumReservation reservation = stadiumReservationRepository
                .findById(reservationId).orElseThrow(() ->
                        new StadiumException(ReservationNotFound));

        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (!reservation.getStadium().getId().equals(stadiumId)) {
            throw new StadiumException(StadiumReservationNotMatch);
        }

        if (!reservation.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        reservation.cancelReservation();
        StadiumReservationCancel reservationCancel = StadiumReservationCancel.builder()
                .reservation(reservation)
                .price(reservation.getPrice())
                .build();
        stadiumReservationCancelRepository.save(reservationCancel);
    }

    @Transactional
    public ReservationInfoResponse createReservation(
            Member member,
            Long stadiumId,
            CreateReservationRequest request
    ) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        // Get Lock
        try {
            redissonLockReservingTimeFacade.lock(stadiumId, request.getReservingDate());
        } catch (StadiumException e) {
            throw e;
        }

        // Check Already Reserved Times
        if (isAlreadyReservedTimes(stadium, request.getReservingDate(), request.getReservingTimes())) {
            throw new StadiumException(AlreadyReservedTime);
        }

        // Create Reservation
        StadiumReservation reservation = StadiumReservation
                .fromRequest(stadium, member, request, getPrice(stadiumId, request.getReservingDate(), request).getPrice());

        // Save Reservation
        stadiumReservationRepository.save(reservation);

        // Create Item Reservation
        List<StadiumReservationItem> items = new ArrayList<>();
        if (request.getItems().size() > 0) {
            request.getItems().forEach(item -> {
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
        redissonLockReservingTimeFacade.unlock(stadiumId, request.getReservingDate());

        return ReservationInfoResponse.builder()
                .id(reservation.getId())
                .openTime(stadium.getOpenTime())
                .closeTime(stadium.getCloseTime())
                .stadiumId(stadiumId)
                .stadiumName(stadium.getName())
                .reservedTimes(reservation.getReservingTimes().stream()
                        .map(ReservingTime::getTime)
                        .collect(Collectors.toList()))
                .pricePerHalfHour(reservation.getPrice())
                .date(reservation.getReservingDate())
                .items(items.stream()
                        .map(ItemResponse::fromReservationItem)
                        .collect(Collectors.toList()))
                .build();
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

    // TODO: Check Price
    public PriceResponse getPrice(
            Long stadiumId,
            LocalDate date,
            CreateReservationRequest request
    ) {
        return PriceResponse.builder()
                .price(10000)
                .build();
    }
}
