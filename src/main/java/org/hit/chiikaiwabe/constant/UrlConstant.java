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

}
