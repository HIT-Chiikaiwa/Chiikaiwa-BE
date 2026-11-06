package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, String> {
    Page<AdminActionLog> findByTarget_Id(String userId, Pageable pageable);
}
