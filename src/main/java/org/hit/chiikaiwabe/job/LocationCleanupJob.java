package org.hit.chiikaiwabe.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.config.properties.RadarProperties;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.service.LocationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationCleanupJob {
    private static final String TIME_KEY = "buddy_timestamps";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RadarProperties radarProperties;
    private final LocationService locationService;

    @Scheduled(fixedRateString = "60000")
    public void cleanupStaleLocation(){
        log.debug(SuccessMessage.CleanupLocation.PROCESSING);
        long currentTimeMillis = System.currentTimeMillis();
        long timeToLiveMillis = (long)radarProperties.getTtlMinutes() * 60 * 1000;

        Map<Object, Object> timestamps = redisTemplate.opsForHash().entries(TIME_KEY);

        if(timestamps.isEmpty()){
            return;
        }

        int countDeleteUser = 0;
        for(Map.Entry<Object, Object> entry : timestamps.entrySet()){
            String userId = entry.getKey().toString();
            long lastActiveTimestamp = Long.parseLong(entry.getValue().toString());

            if(currentTimeMillis - lastActiveTimestamp > timeToLiveMillis){
                locationService.removeLocation(userId);
                countDeleteUser++;
                log.info(SuccessMessage.CleanupLocation.SUCCESS + "userID: " + userId);
            }
        }
        log.debug(SuccessMessage.CleanupLocation.TOTAL_COUNT_SUCCESS + countDeleteUser + "userID");
    }

}
