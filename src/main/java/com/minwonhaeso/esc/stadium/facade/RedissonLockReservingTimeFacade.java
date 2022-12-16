package com.minwonhaeso.esc.stadium.facade;

import com.minwonhaeso.esc.error.exception.StadiumException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.AlreadyReservedTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedissonLockReservingTimeFacade {
    private final RedissonClient redissonClient;

    public void lock(Long stadiumId, LocalDate date) {
        RLock lock = redissonClient.getLock(getLockKey(stadiumId, date));
        log.debug("Trying lock for stadium id : {}", stadiumId);

        try {
            boolean isLock = lock.tryLock(1, 1, TimeUnit.SECONDS);
            if(!isLock) {
                log.error("======Lock acquisition failed=====");
                throw new StadiumException(AlreadyReservedTime);
            }
        } catch (StadiumException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed");
        }
    }

    public void unlock(Long stadiumId, LocalDate date) {
        log.debug("Unlock for stadium id : {}", stadiumId);
        redissonClient.getLock(getLockKey(stadiumId, date)).unlock();
    }

    private static String getLockKey(Long stadiumId, LocalDate date) {
        return "RTL-" + stadiumId + "-" + date;
    }
}
