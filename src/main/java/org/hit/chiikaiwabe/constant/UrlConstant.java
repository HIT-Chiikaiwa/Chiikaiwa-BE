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

}
