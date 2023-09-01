package com.minwonhaeso.esc.stadium.service;

import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.stadium.facade.RedissonLockReservingTimeFacade;
import com.minwonhaeso.esc.stadium.model.dto.StadiumInfoResponseDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.*;
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
    public Page<ReservationResponse> getAllReservationsByMember(
            Member member, Pageable pageable) {
        return stadiumReservationRepository
                .findAllByMemberOrderByReservingDateDesc(member, pageable)
                .map(ReservationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReservationStadiumInfoResponse getStadiumReservationInfo(
            Long stadiumId, LocalDate date) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

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

        return ReservationStadiumInfoResponse.builder()
                .openTime(stadium.getOpenTime().getTime())
                .closeTime(stadium.getCloseTime().getTime())
                .stadium(StadiumInfoResponseDto.fromEntity(stadium))
                .date(date.toString())
                .pricePerHalfHour(isHoliday(date)
                        ? stadium.getHolidayPricePerHalfHour()
                        : stadium.getWeekdayPricePerHalfHour())
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

        return ReservationInfoResponse.fromEntity(reservation);
    }

    @Transactional(readOnly = true)
    public Page<StadiumReservationUserResponse> getAllReservationUsersByManager (
            Member member, Long stadiumId, Pageable pageable
    ) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (!stadium.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StadiumException(UnAuthorizedAccess);
        }


        return stadiumReservationRepository
                .findAllByStadiumOrderByReservingDateDesc(stadium, pageable)
                .map(StadiumReservationUserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getReservationInfoByManager(
            Member member,
            Long stadiumId,
            Long reservationId
    ) {
        StadiumReservation reservation = stadiumReservationRepository
                .findById(reservationId).orElseThrow(() ->
                        new StadiumException(ReservationNotFound));

        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        if (!reservation.getStadium().getId().equals(stadiumId)) {
            throw new StadiumException(StadiumReservationNotMatch);
        }

        if (!stadium.getMember().getMemberId().equals(member.getMemberId())) {
            throw new StadiumException(UnAuthorizedAccess);
        }

        return ReservationInfoResponse.fromEntity(reservation);
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

        if (reservation.getStatus() != StadiumReservationStatus.RESERVED) {
            throw new StadiumException(CouldNotCancelReservation);
        }

        reservation.cancelReservation();
        StadiumReservationCancel reservationCancel = StadiumReservationCancel.builder()
                .reservation(reservation)
                .price(reservation.getPrice())
                .build();

        stadiumReservationRepository.save(reservation);
        stadiumReservationCancelRepository.save(reservationCancel);
    }

    @Transactional
    public CreateReservationResponse createReservation(
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
        List<StadiumReservationItem> rentalItems = new ArrayList<>();
        if (request.getItems().size() > 0) {
            request.getItems().forEach(item -> {
                try {
                    StadiumItem stadiumItem = stadiumItemRepository.findById(item.getItemId())
                            .orElseThrow(() -> new StadiumException(ItemNotFound));

                    rentalItems.add(StadiumReservationItem.builder()
                            .item(stadiumItem)
                            .count(item.getCount())
                            .reservation(reservation)
                            .price(item.getCount() * stadiumItem.getPrice())
                            .build());
                } catch (StadiumException e) {
                    log.info("item not found. id:" + item.getItemId());
                }
            });
            reservation.getItems().addAll(rentalItems);
            stadiumReservationItemRepository.saveAll(rentalItems);
        }

        stadiumReservationRepository.save(reservation);
        redissonLockReservingTimeFacade.unlock(stadiumId, request.getReservingDate());

        return CreateReservationResponse.builder()
                .reservationId(reservation.getId())
                .openTime(stadium.getOpenTime().getTime())
                .closeTime(stadium.getCloseTime().getTime())
                .stadiumId(stadium.getId())
                .stadiumName(stadium.getName())
                .reservedTimes(reservation.getReservingTimes().stream()
                        .map(ReservingTime::getTime)
                        .collect(Collectors.toList()))
                .pricePerHalfHour(reservation.getPrice())
                .date(reservation.getReservingDate().toString())
                .rentalItems(rentalItems.stream()
                        .map(ItemResponse::fromReservationItem)
                        .collect(Collectors.toList()))
                .build();
    }

    public void executeReservation(Member member, Long stadiumId, Long reservationId) {
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

        if (reservation.getReservingDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new StadiumException(TooEarlyExecute);
        }

        reservation.executeReservation();
        stadiumReservationRepository.save(reservation);
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
            try {
                if (reservedTimes.contains(ReservingTime.findTime(time))) {
                    return true;
                }
            } catch (Exception e) {
                throw new StadiumException(TimeFormatNotAccepted);
            }
        }

        return false;
    }

    public PriceResponse getPrice(
            Long stadiumId,
            LocalDate date,
            CreateReservationRequest request
    ) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new StadiumException(StadiumNotFound));

        int stadiumPrice = isHoliday(date) ?
                stadium.getHolidayPricePerHalfHour() * request.getReservingTimes().size()
                : stadium.getWeekdayPricePerHalfHour() * request.getReservingTimes().size();

        int itemPrice = request.getItems().stream().map(item -> {
            StadiumItem stadiumItem = stadiumItemRepository.findById(item.getItemId())
                    .orElseThrow(() -> new StadiumException(StadiumItemNotFound));

            return stadiumItem.getPrice() * item.getCount();
        }).reduce(0, Integer::sum);

        return PriceResponse.builder()
                .price(stadiumPrice + itemPrice)
                .build();
    }
}
