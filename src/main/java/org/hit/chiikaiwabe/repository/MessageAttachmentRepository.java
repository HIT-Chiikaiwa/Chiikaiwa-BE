package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {
}
