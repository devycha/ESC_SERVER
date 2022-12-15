package com.minwonhaeso.esc.stadium.facade;

import com.minwonhaeso.esc.error.exception.StadiumException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.minwonhaeso.esc.error.type.StadiumErrorCode.AlreadyReservedTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedissonLockReservingTimeFacade {
    private final RedissonClient redissonClient;

    public String lock(Long stadiumId, String reservingTime) {
        String key = getLockKey(stadiumId, reservingTime);
        RLock lock = redissonClient.getLock(key);
        log.debug("Trying lock for accountNumber : {}", reservingTime);

        try {
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
            if(!isLock) {
                log.error("======Lock acquisition failed=====");
                throw new StadiumException(AlreadyReservedTime);
            }
        } catch (StadiumException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed");
        }

        return key;
    }

    public void unlock(String key) {
        log.debug("Unlock for key : {}", key);
        redissonClient.getLock(key).unlock();
    }

    private static String getLockKey(Long stadiumId, String accountNumber) {
        return "RTL-" + stadiumId + "-" + accountNumber;
    }
}
