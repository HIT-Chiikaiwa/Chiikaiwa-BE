package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReactionSummaryDto;

import java.util.List;

public interface MessageReactionService {

    CommonResponseDto addReaction(String userId, String messageId, String emoji);

    CommonResponseDto removeReaction(String userId, String messageId);

    List<ReactionSummaryDto> getReactions(String messageId);

}
