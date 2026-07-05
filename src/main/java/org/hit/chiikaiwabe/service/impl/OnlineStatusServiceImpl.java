package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OnlineStatusServiceImpl implements OnlineStatusService {

    @Override
    public void setOnline(String userId) { /* TODO */ }

    @Override
    public boolean isOnline(String userId) { return false; /* TODO */ }

    @Override
    public void setTyping(String conversationId, String userId) { /* TODO */ }

    @Override
    public void incrementUnread(String conversationId, String userId) { /* TODO */ }

    @Override
    public void resetUnread(String conversationId, String userId) { /* TODO */ }

    @Override
    public int getUnreadCount(String conversationId, String userId) { return 0; /* TODO */ }
}
