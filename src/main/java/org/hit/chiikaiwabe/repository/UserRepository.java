package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  @Query("SELECT u FROM User u WHERE u.id = ?1")
  Optional<User> findById(String id);

  @Query("SELECT u FROM User u WHERE u.username = ?1")
  Optional<User> findByUsername(String username);

  @Query("SELECT u FROM User u WHERE u.email = ?1")
  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.phone = ?1")
  Optional<User> findByPhoneNumber(String phone);

  @Query("SELECT u FROM User u WHERE u.deleteFlag = false AND (u.phone = ?1 OR u.email = ?1)")
  Optional<User> findByPhoneOrEmail(String keyword);

  @Query("SELECT u FROM User u WHERE u.deleteFlag = false AND " +
          "(LOWER(u.firstName) LIKE :keyword OR LOWER(u.lastName) LIKE :keyword " +
          "OR LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE :keyword)")
  List<User> searchByName(@Param("keyword") String keyword);




  boolean existsByEmail(String email);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET u.trustScore = (COALESCE(u.trustScore, 0.0) * COALESCE(u.totalRatingCount, 0) + :newScore) / (COALESCE(u.totalRatingCount, 0) + 1.0), u.totalRatingCount = COALESCE(u.totalRatingCount, 0) + 1 WHERE u.id = :userId")
  void updateTrustScoreMovingAverage(@Param("userId") String userId, @Param("newScore") Integer newScore);

  List<User> findAllByIdInAndDeleteFlagFalseAndBuddyActiveTrue(List<String> ids);

  default User getUser(UserPrincipal currentUser) {
    return findByUsername(currentUser.getUsername())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                    new String[]{currentUser.getUsername()}));
  }



  @Query("SELECT u FROM User u WHERE u.deleteFlag = false ORDER BY u.expPoints DESC")
  Page<User> findTopByExpPoints(Pageable pageable);

  @Query("SELECT COUNT(u)  FROM User u WHERE u.deleteFlag = false AND u.expPoints > :targetExp")
  long getUserRank(@Param("targetExp") long targetExp);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE User u SET " +
          "u.expPoints = GREATEST(0, u.expPoints + :delta), " +
          "u.title = CASE " +
          "  WHEN GREATEST(0, u.expPoints + :delta) >= 1000 THEN 'Lão Làng' " +
          "  WHEN GREATEST(0, u.expPoints + :delta) >= 800 THEN 'Cộng Sự Siêu Đẳng' " +
          "  WHEN GREATEST(0, u.expPoints + :delta) >= 600 THEN 'Có Công Mài Sắt' " +
          "  WHEN GREATEST(0, u.expPoints + :delta) >= 200 THEN 'Tân Binh Kỳ Cựu' " +
          "  ELSE 'Siêu Tân Binh' END " +
          "WHERE u.id = :userId")
  void updateExpPoints(@Param("userId") String userId, @Param("delta") int delta);

}

