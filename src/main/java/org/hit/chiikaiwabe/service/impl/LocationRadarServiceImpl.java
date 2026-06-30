package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.config.properties.RadarProperties;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.response.NearbyUserDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.mapper.LocationMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.LocationRadarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LocationRadarServiceImpl implements LocationRadarService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final LocationMapper locationMapper;
    private final RadarProperties radarProperties;

    private static final String BUDDY_LOCATIONS_KEY = "buddy_locations";
    private static final String TIME_KEY = "buddy_timestamps";

    @Override
    @Transactional(readOnly = true)
    public void updateLocation(String userId, double lat, double lon) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));

        if (!user.getBuddyActive()) {
            throw new ForbiddenException(ErrorMessage.Location.ERR_BUDDY_INACTIVE);
        }

        redisTemplate.opsForGeo().add(BUDDY_LOCATIONS_KEY, new Point(lon, lat), userId);
        redisTemplate.opsForHash().put(TIME_KEY, userId, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void removeLocation(String userId) {
        redisTemplate.opsForGeo().remove(BUDDY_LOCATIONS_KEY, userId);
        redisTemplate.opsForHash().delete(TIME_KEY, userId);
    }

    @Override
    public List<NearbyUserDto> scanRadar(String userId, double lat, double lng, Double radiusKm) {
        User scanner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
        if (!scanner.getBuddyActive()) {
            throw new ForbiddenException(ErrorMessage.Location.ERR_BUDDY_INACTIVE);
        }

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new InvalidException(ErrorMessage.Location.ERR_INVALID_COORDINATES);
        }

        double searchRadius = radarProperties.getDefaultRadiusKm();
        if (radiusKm != null && radiusKm > 0) {
            searchRadius = Math.min(radiusKm, radarProperties.getMaxRadiusKm());
        }

        Distance distance = new Distance(searchRadius, Metrics.KILOMETERS);
        Circle circle = new Circle(new Point(lng, lat), distance);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending()
                .limit(50);

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
                redisTemplate.opsForGeo().radius(BUDDY_LOCATIONS_KEY, circle, args);

        if (geoResults == null) {
            return new ArrayList<>();
        }

        List<String> nearbyUserIds = new ArrayList<>();
        Map<String, double[]> locationDataMap = new HashMap<>();
        Map<String, Double> distanceMap = new HashMap<>();

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : geoResults) {
            String targetUserId = result.getContent().getName();

            if (targetUserId.equals(userId)) {
                continue;
            }

            nearbyUserIds.add(targetUserId);
            distanceMap.put(targetUserId, result.getDistance().getValue());

            Point point = result.getContent().getPoint();
            if (point != null) {
                locationDataMap.put(targetUserId, new double[]{
                        obfuscate(point.getY()),
                        obfuscate(point.getX())
                });
            }
        }

        if (nearbyUserIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = userRepository.findAllByIdInAndDeleteFlagFalseAndBuddyActiveTrue(nearbyUserIds);

        List<NearbyUserDto> result = new ArrayList<>();
        for (User user : users) {
            NearbyUserDto dto = locationMapper.toNearbyUserDto(user);

            Double dist = distanceMap.get(user.getId());
            if (dist != null) {
                dto.setDistanceKm(dist);
            }

            double[] coords = locationDataMap.get(user.getId());
            if (coords != null) {
                dto.setLatitude(coords[0]);
                dto.setLongitude(coords[1]);
            }

            result.add(dto);
        }

        result.sort((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()));

        return result;
    }

    private double obfuscate(double coordinate) {
        double offset = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * 0.0007;
        return Math.round((coordinate + offset) * 10000.0) / 10000.0;
    }

}
