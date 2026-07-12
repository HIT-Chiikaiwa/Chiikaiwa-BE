package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReactionSummaryDto;

import java.util.List;

public interface MessageReactionService {

    void addReaction(String userId, String messageId, String emoji);

    void removeReaction(String userId, String messageId);

    List<ReactionSummaryDto> getReactions(String messageId);

}
