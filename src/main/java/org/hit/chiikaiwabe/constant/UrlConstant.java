package org.hit.chiikaiwabe.constant;

public class UrlConstant {

  public static class Auth {
    private static final String PRE_FIX = "/auth";

    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGOUT = PRE_FIX + "/logout";

    public static final String REFRESH_TOKEN = PRE_FIX + "/refresh";

    public static final String REGISTER = PRE_FIX + "/register";
    public static final String SEND_OTP = PRE_FIX + "/send-otp";
    public static final String FORGOT_PASSWORD_SEND_OTP = PRE_FIX + "/forgot-password/send-otp";
    public static final String FORGOT_PASSWORD_VERIFY_OTP = PRE_FIX + "/forgot-password/verify-otp";
    public static final String FORGOT_PASSWORD_RESET = PRE_FIX + "/forgot-password/reset";
    public static final String VERIFY_REGISTER_OTP = PRE_FIX + "/verify-register-otp";
    private Auth() {
    }
  }

  public static class User {
    private static final String PRE_FIX = "/user";
    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{userId}";
    public static final String GET_USER_ONLINE = GET_USER + "/online-status";
    public static final String GET_CURRENT_USER = PRE_FIX + "/current";


    private User() {
    }
  }

  public static class Profile {
    private static final String PRE_FIX = "/profile";

    public static final String GET_PROFILE = PRE_FIX + "/{userId}";
    public static final String UPDATE_PERSONAL_INFO = PRE_FIX + "/{userId}/personal-info";
    public static final String CHANGE_PASSWORD = PRE_FIX + "/{userId}/password";
    public static final String UPLOAD_AVATAR = PRE_FIX + "/{userId}/avatar";
    public static final String DELETE_USER = PRE_FIX + "/{userId}";

    public static final String UPDATE_ACADEMIC_INFO = PRE_FIX + "/{userId}/academic-info";
    public static final String ADD_SUBJECT = PRE_FIX + "/{userId}/subjects";
    public static final String GET_SUBJECTS = PRE_FIX + "/{userId}/subjects";
    public static final String DELETE_SUBJECT = PRE_FIX + "/{userId}/subjects/{subjectId}";

    public static final String UPDATE_BUDDY_STATUS = PRE_FIX + "/{userId}/status";
    public static final String UPDATE_STATUS_TAG = PRE_FIX + "/{userId}/status-tag";
    public static final String UPDATE_LOCATION = PRE_FIX + "/{userId}/location";

    private Profile() {
    }
  }
  public static class Location {
    private static final String PRE_FIX = "/location";

    public static final String UPDATE_GPS = PRE_FIX + "/update";
    public static final String REMOVE_GPS = PRE_FIX + "/remove";
    public static final String RADAR = PRE_FIX + "/radar";

    private Location() {
    }
  }

  public static class Chat {
    private static final String PRE_FIX = "/chat";
    public static final String CONVERSATIONS = PRE_FIX + "/conversations";
    public static final String DIRECT = CONVERSATIONS + "/direct";
    public static final String GROUP = CONVERSATIONS + "/group";
    public static final String MESSAGES = "/{id}/messages";
    public static final String UPLOAD = "/{id}/upload";
    public static final String SCHEDULE_INVITE = "/{id}/schedule-invite";
    public static final String MEMBERS = "/{id}/members";
    public static final String MEMBER_DETAIL = "/{id}/members/{userId}";

    public static final String SEARCH_CONVERSATIONS = PRE_FIX + "/conversations/search";
    public static final String SEARCH_MESSAGES = CONVERSATIONS + "/{id}/messages/search";
    public static final String PIN_MESSAGE = PRE_FIX + "/messages/{msgId}/pin";
    public static final String UNPIN_MESSAGE = PRE_FIX + "/messages/{msgId}/unpin";
    public static final String PINNED_MESSAGES = CONVERSATIONS + "/{id}/pinned";
    public static final String REPLY_MESSAGE = PRE_FIX + "/messages/{msgId}/reply";
    public static final String FORWARD_MESSAGE = PRE_FIX + "/messages/{msgId}/forward";
    public static final String MESSAGE_REACTIONS = PRE_FIX + "/messages/{msgId}/reactions";
    public static final String DELETE_MESSAGE = PRE_FIX + "/messages/{msgId}";
    public static final String RECALL_MESSAGE = PRE_FIX + "/messages/{msgId}/recall";
    public static final String DISSOLVE_GROUP = CONVERSATIONS + "/{id}/dissolve";
    public static final String TRANSFER_OWNERSHIP = CONVERSATIONS + "/{id}/transfer-ownership";

    private Chat() {
    }
  }

  public static class BlockReport {
    public static final String BLOCK = "/users/block/{userId}";
    public static final String BLOCKED_LIST = "/users/blocked";
    public static final String REPORT = "/reports";

    private BlockReport() {
    }
  }

  public static class Device {
    public static final String DEVICES = "/devices";
    public static final String UPDATE_DEVICE = DEVICES;

    private Device() {
    }
  }

  public static class Friendship {
    private static final String PRE_FIX = "/friends";
    public static final String FRIENDS_LIST = PRE_FIX;
    public static final String SEND_REQUEST = PRE_FIX + "/request/{targetUserId}";
    public static final String ACCEPT_REQUEST = PRE_FIX + "/request/{requestId}/accept";
    public static final String REJECT_REQUEST = PRE_FIX + "/request/{requestId}/reject";
    public static final String UNFRIEND = PRE_FIX + "/{friendId}";
    public static final String PENDING_REQUESTS = PRE_FIX + "/requests/pending";
    public static final String SEARCH_FRIENDS = PRE_FIX + "/search";

    private Friendship() {
    }
  }

  public static class UserSearch {
    public static final String SEARCH_BY_PHONE = "/users/search/phone";

    private UserSearch() {
    }
  }

  public static class Booking {
    private static final String PRE_FIX = "/bookings";
    public static final String CREATE = PRE_FIX + "/conversation/{conversationId}";
    public static final String ACCEPT = PRE_FIX + "/{bookingId}/accept";
    public static final String REJECT = PRE_FIX + "/{bookingId}/reject";
    public static final String CANCEL = PRE_FIX + "/{bookingId}/cancel";
    public static final String COMPLETE = PRE_FIX + "/{bookingId}/complete";
    public static final String RATE = PRE_FIX + "/{bookingId}/rate";
    public static final String LIST = PRE_FIX;
    public static final String DETAIL = PRE_FIX + "/{bookingId}";
    public static final String WEEKLY = PRE_FIX + "/weekly";

    private Booking() {
    }
  }
  public static class Leaderboard {
    private static final String PRE_FIX = "/leaderboard";
    public static final String TOP = PRE_FIX + "/top";
    public static final String MY_RANK = PRE_FIX + "/me";
    public static final String HISTORY = PRE_FIX + "/history";

    private Leaderboard() {
    }
  }

}
