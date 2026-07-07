package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, String> {
}
