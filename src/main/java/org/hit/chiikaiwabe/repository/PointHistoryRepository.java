package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, String> {

    Page<PointHistory> findByUserIdOrderByCreatedDateDesc(String userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM PointHistory p WHERE p.user.id = :userId " +
            "AND p.action = org.hit.chiikaiwabe.domain.enums.PointAction.DAILY_FIRST_MESSAGE " +
            "AND p.createdDate >= :startOfDay")
    long countDailyFirstMessage(@Param("userId") String userId,
                                @Param("startOfDay") LocalDateTime startOfDay);
}
