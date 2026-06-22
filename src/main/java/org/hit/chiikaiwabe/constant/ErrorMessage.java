package org.hit.chiikaiwabe.constant;

public class ErrorMessage {

  public static final String ERR_EXCEPTION_GENERAL = "exception.general";
  public static final String UNAUTHORIZED = "exception.unauthorized";
  public static final String FORBIDDEN = "exception.forbidden";
  public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

  //error validation dto
  public static final String INVALID_SOME_THING_FIELD = "invalid.general";
  public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
  public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
  public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
  public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
  public static final String INVALID_DATE = "invalid.date-format";
  public static final String INVALID_DATE_FEATURE = "invalid.date-future";
  public static final String INVALID_DATETIME = "invalid.datetime-format";

  public static class Auth {
    public static final String ERR_INCORRECT_USERNAME = "exception.auth.incorrect.username";
    public static final String ERR_INCORRECT_PASSWORD = "exception.auth.incorrect.password";
    public static final String ERR_ACCOUNT_NOT_ENABLED = "exception.auth.account.not.enabled";
    public static final String ERR_ACCOUNT_LOCKED = "exception.auth.account.locked";
    public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
    public static final String EXPIRED_REFRESH_TOKEN = "exception.auth.expired.refresh.token";
    public static final String ERR_CONFIRM_PASSWORD_NOT_MATCH = "auth.register.password_not_match";
    public static final String ERR_ACCOUNT_ALREADY_EXISTS = "auth.register.account_exists";
    public static final String INVALID_FORMAT_PASSWORD_COMPLEX = "invalid.password-complex";
    public static final String ERR_RESET_TICKET_EXPIRED = "exception.auth.reset.ticket.expired";
    public static final String ERR_OTP_INCORRECT = "exception.auth.otp.incorrect";
    public static final String ERR_ACCOUNT_ALREADY_VERIFIED = "auth.register.already_verified";
    public static final String ERR_ACCOUNT_NOT_VERIFIED = "exception.auth.account_not_verified";
    public static final String ERR_OTP_SPAM = "exception.auth.spam_otp";
    public static final String ERR_FORGOT_PASS_NOT_VERIFIED = "exception.auth.forgot_pass_not_verified";


  }

  public static class User {
    public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
    public static final String ERR_NOT_FOUND_ID = "exception.user.not.found.id";
  }

  public static class Admin {
    public static final String ERR_NOT_FiND_NAME = "exception.user.not.find.name";
  }


  public static class Role {
    public static final String ERR_ROLE_NOT_FOUND = "exception.role.not.found";
  }
  public static class Mail {
    public static final String ERR_SEND_MAIL_FAILED = "exception.mail.send_failed";
  }


}
