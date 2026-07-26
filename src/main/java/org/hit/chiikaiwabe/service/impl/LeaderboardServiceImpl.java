package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationResponseDto;
import org.hit.chiikaiwabe.domain.dto.pagination.PagingMeta;
import org.hit.chiikaiwabe.domain.dto.response.LeaderboardEntryDto;
import org.hit.chiikaiwabe.domain.dto.response.PointHistoryDto;
import org.hit.chiikaiwabe.domain.dto.response.UserRankDto;
import org.hit.chiikaiwabe.domain.entity.PointHistory;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.PointAction;
import org.hit.chiikaiwabe.domain.enums.UserTitle;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.PointHistoryRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.LeaderboardService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ZSET_KEY = "leaderboard_zset";

    @Override
    @Transactional
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public void awardPoints(String userId, PointAction action, String referenceId) {
        userRepository.updateExpPoints(userId, action.getPoints());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        redisTemplate.opsForZSet().incrementScore(ZSET_KEY, userId, action.getPoints());

        PointHistory history = PointHistory.builder()
                .user(user)
                .action(action)
                .points(action.getPoints())
                .expAfter(user.getExpPoints())
                .referenceId(referenceId)
                .build();
        pointHistoryRepository.save(history);

        log.info("User {} awarded {} EXP for {} [ref={}]. Total EXP: {}",
                userId, action.getPoints(), action.name(), referenceId, user.getExpPoints());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<LeaderboardEntryDto> getLeaderboard(int page, int size) {
        long start = (long) page * size;
        long end = start + size - 1;

  
        Long totalInZSet = redisTemplate.opsForZSet().zCard(ZSET_KEY);
        boolean zsetIsEmpty = (totalInZSet == null || totalInZSet == 0);

        if (!zsetIsEmpty) {
            Set<Object> userIdsRaw = redisTemplate.opsForZSet().reverseRange(ZSET_KEY, start, end);
            List<String> userIds = (userIdsRaw != null)
                    ? userIdsRaw.stream().map(Object::toString).collect(Collectors.toList())
                    : new ArrayList<>();

            List<LeaderboardEntryDto> entries = new ArrayList<>();

            if (!userIds.isEmpty()) {
                List<User> users = userRepository.findAllById(userIds);
                Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
                long rank = start + 1;

                for (String uid : userIds) {
                    User u = userMap.get(uid);
                    if (u != null) {
                        UserTitle title = UserTitle.fromExp(u.getExpPoints());
                        entries.add(LeaderboardEntryDto.builder()
                                .rank(rank++)
                                .userId(u.getId())
                                .firstName(u.getFirstName())
                                .lastName(u.getLastName())
                                .avatar(u.getAvatar())
                                .expPoints(u.getExpPoints())
                                .title(title.getDisplayName())
                                .titleIcon(title.getIcon())
                                .build());
                    }
                }
            }

            int totalPages = (int) Math.ceil((double) totalInZSet / size);
            PagingMeta meta = new PagingMeta(totalInZSet, totalPages, page, size, "expPoints", "DESC");
            return new PaginationResponseDto<>(meta, entries);
        }

        log.warn("Leaderboard ZSET is empty, falling back to Database query.");
        return getLeaderboardFromDb(page, size);
    }

    private PaginationResponseDto<LeaderboardEntryDto> getLeaderboardFromDb(int page, int size) {
        Page<User> users = userRepository.findTopByExpPoints(PageRequest.of(page, size));

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        long startRank = (long) page * size + 1;

        for (int i = 0; i < users.getContent().size(); i++) {
            User u = users.getContent().get(i);
            UserTitle title = UserTitle.fromExp(u.getExpPoints());
            entries.add(LeaderboardEntryDto.builder()
                    .rank(startRank + i)
                    .userId(u.getId())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .avatar(u.getAvatar())
                    .expPoints(u.getExpPoints())
                    .title(title.getDisplayName())
                    .titleIcon(title.getIcon())
                    .build());
        }

        PagingMeta meta = new PagingMeta(
                users.getTotalElements(),
                users.getTotalPages(),
                page,
                size,
                "expPoints",
                "DESC");

        return new PaginationResponseDto<>(meta, entries);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserRankNumber(String userId, Long expPoints) {
        Long zRank = redisTemplate.opsForZSet().reverseRank(ZSET_KEY, userId);
        if (zRank != null) {
            return zRank + 1;
        }
        log.warn("User {} not found in ZSET, falling back to Database rank query.", userId);
        return userRepository.getUserRank(expPoints);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRankDto getUserRank(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        Long zRank = redisTemplate.opsForZSet().reverseRank(ZSET_KEY, userId);

        long rank;
        long totalUsers;

        if (zRank != null) {
            rank = zRank + 1;
            Long zCard = redisTemplate.opsForZSet().zCard(ZSET_KEY);
            totalUsers = (zCard != null) ? zCard : userRepository.count();
        } else {
            log.warn("User {} not found in ZSET, falling back to Database rank query.", userId);
            rank = userRepository.getUserRank(user.getExpPoints());
            totalUsers = userRepository.count();
        }

        UserTitle currentTitle = UserTitle.fromExp(user.getExpPoints());
        UserTitle nextTitle = currentTitle.next();

        double progressPercent;
        if (nextTitle != null) {
            long range = nextTitle.getMinExp() - currentTitle.getMinExp();
            long progress = user.getExpPoints() - currentTitle.getMinExp();
            progressPercent = range > 0 ? (double) progress / range * 100.0 : 100.0;
        } else {
            progressPercent = 100.0;
        }

        return UserRankDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .expPoints(user.getExpPoints())
                .title(currentTitle.getDisplayName())
                .titleIcon(currentTitle.getIcon())
                .rank(rank)
                .totalUsers(totalUsers)
                .nextTitleMinExp(nextTitle != null ? (long) nextTitle.getMinExp() : null)
                .nextTitle(nextTitle != null ? nextTitle.getDisplayName() : null)
                .nextTitleIcon(nextTitle != null ? nextTitle.getIcon() : null)
                .progressPercent(Math.min(progressPercent, 100.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<PointHistoryDto> getPointHistory(String userId, int page, int size) {
        Page<PointHistory> histories = pointHistoryRepository
                .findByUserIdOrderByCreatedDateDesc(userId, PageRequest.of(page, size));

        List<PointHistoryDto> dtos = histories.getContent().stream()
                .map(h -> PointHistoryDto.builder()
                        .id(h.getId())
                        .action(h.getAction().name())
                        .actionDescription(h.getAction().getDescription())
                        .points(h.getPoints())
                        .expAfter(h.getExpAfter())
                        .createdDate(h.getCreatedDate())
                        .build())
                .toList();

        PagingMeta meta = new PagingMeta(
                histories.getTotalElements(),
                histories.getTotalPages(),
                page,
                size,
                "createdDate",
                "DESC");

        return new PaginationResponseDto<>(meta, dtos);
    }
}
