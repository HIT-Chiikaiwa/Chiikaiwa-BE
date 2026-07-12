package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(String blockerId, String blockedId);
    List<UserBlock> findByBlockerId(String blockerId);
    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);
}
