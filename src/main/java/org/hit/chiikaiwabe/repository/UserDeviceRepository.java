package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    @Query("SELECT d FROM UserDevice d WHERE d.user.id = ?1 AND d.isActive = true")
    List<UserDevice> findByUserIdAndIsActiveTrue(String userId);

    @Query("SELECT d FROM UserDevice d WHERE d.fcmToken = ?1")
    Optional<UserDevice> findByFcmToken(String fcmToken);

    @Query("SELECT d FROM UserDevice d WHERE d.user.id = ?1 AND d.fcmToken = ?2")
    Optional<UserDevice> findByUserIdAndFcmToken(String userId, String fcmToken);
}
