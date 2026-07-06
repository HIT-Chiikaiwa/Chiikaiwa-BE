package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.conversation WHERE m.id = :id")
    Optional<Message> findByIdWithDetails(@Param("id") String id);
}
