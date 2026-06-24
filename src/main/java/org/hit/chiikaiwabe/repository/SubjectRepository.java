package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, String> {

    @Query("SELECT s FROM Subject s WHERE s.user.id = ?1 ORDER BY s.createdDate DESC")
    List<Subject> findAllByUserId(String userId);

    @Query("SELECT s FROM Subject s WHERE s.user.id = ?1 AND s.type = ?2")
    List<Subject> findByUserIdAndType(String userId, String type);

    @Query("SELECT s FROM Subject s WHERE s.user.id = ?1 AND LOWER(s.name) = LOWER(?2)")
    List<Subject> findByUserIdAndName(String userId, String name);

}
