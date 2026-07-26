package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Friendship;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.id = :userId1 AND f.receiver.id = :userId2) OR " +
            "(f.requester.id = :userId2 AND f.receiver.id = :userId1)")
    Optional<Friendship> findFriendshipBetween(@Param("userId1") String userId1,
                                               @Param("userId2") String userId2);

    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.requester JOIN FETCH f.receiver " +
            "WHERE (f.requester.id = :userId OR f.receiver.id = :userId) " +
            "AND f.status = 'ACCEPTED'")
    Page<Friendship> findAcceptedFriendsByUserId(@Param("userId") String userId, Pageable pageable);
    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.requester JOIN FETCH f.receiver " +
            "WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
    Page<Friendship> findPendingRequestsForUser(@Param("userId") String userId, Pageable pageable);
    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.requester JOIN FETCH f.receiver " +
            "WHERE (f.requester.id = :userId OR f.receiver.id = :userId) " +
            "AND f.status = 'ACCEPTED' " +
            "AND (LOWER(CASE WHEN f.requester.id = :userId THEN f.receiver.firstName ELSE f.requester.firstName END) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(CASE WHEN f.requester.id = :userId THEN f.receiver.lastName ELSE f.requester.lastName END) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Friendship> searchFriendsByName(@Param("userId") String userId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "f.requester.id = :userId1 AND f.receiver.id = :userId2 " +
            "AND f.status = :status")
    boolean existsByRequesterIdAndReceiverIdAndStatus(@Param("userId1") String userId1,
                                                      @Param("userId2") String userId2,
                                                      @Param("status") FriendshipStatus status);
}
