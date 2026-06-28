package org.hit.chiikaiwabe.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.LocationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.geo.Point;
import org.hit.chiikaiwabe.constant.ErrorMessage;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private static final String GEO_KEY = "buddy_locations";
    private static final String TIME_KEY = "buddy_timestamps";

    @Transactional
    public void updateLocation(String userId, double lat, double lon) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));

        if (!user.getBuddyActive()) {
            throw new ForbiddenException(ErrorMessage.Location.ERR_INACTIVE_STATUS);
        }
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(lon, lat), userId);
        redisTemplate.opsForHash().put(TIME_KEY, userId, String.valueOf(System.currentTimeMillis()));
    }
}
