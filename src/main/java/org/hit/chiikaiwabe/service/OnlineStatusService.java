package org.hit.chiikaiwabe.service;

public interface OnlineStatusService {
    void setOnline(String userID);
    boolean isOnline(String userID);
    void setTyping(String conversationID, String userID);
    void incrementUnread(String conversationID, String userID);
    void resetUnread(String conversationID, String userID);
    int getUnreadCount(String conversationID, String userID);
}
