package org.hit.chiikaiwabe.service;

public interface OnlineStatusService {
    void setOnline(String userId);
    void setOffline(String userId);
    boolean isOnline(String userId);
    void setTyping(String conversationId, String userId);
    void incrementUnread(String conversationId, String userId);
    void resetUnread(String conversationId, String userId);
    int getUnreadCount(String conversationId, String userId);
}
