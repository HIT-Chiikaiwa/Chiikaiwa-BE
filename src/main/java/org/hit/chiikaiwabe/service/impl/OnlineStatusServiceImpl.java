package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ONLINE_KEY = "user:online:";
    private static final String TYPING_KEY = "user:typing:";
    private static final String UNREAD_KEY = "unread:";

    @Override
    public void setOnline(String userID) {
        redisTemplate.opsForValue().set(ONLINE_KEY + userID, "true", Duration.ofMinutes(5));
    }

    @Override
    public void setOffline(String userID) {
        redisTemplate.delete(ONLINE_KEY + userID);
    }

    @Override
    public boolean isOnline(String userID) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_KEY + userID));
    }

    @Override
    public void setTyping(String conversationID, String userID) {
        redisTemplate.opsForValue().set(TYPING_KEY + conversationID +":"+ userID, "true", Duration.ofSeconds(3));
    }

    @Override
    public void incrementUnread(String conversationID, String userID) {
        redisTemplate.opsForValue().increment(UNREAD_KEY + conversationID+":"+userID);
    }

    @Override
    public void resetUnread(String conversationID, String userID) {
        redisTemplate.delete(UNREAD_KEY + conversationID+":"+userID);
    }

    @Override
    public int getUnreadCount(String conversationID, String userID) {
        Object value = redisTemplate.opsForValue().get(UNREAD_KEY + conversationID+":"+userID);
        if(value == null) return 0;
        return Integer.parseInt(value.toString());
    }
}
