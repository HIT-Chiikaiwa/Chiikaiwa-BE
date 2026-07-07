package org.hit.chiikaiwabe.constant;

public class ErrorMessage {

  public static final String ERR_EXCEPTION_GENERAL = "exception.general";
  public static final String UNAUTHORIZED = "exception.unauthorized";
  public static final String FORBIDDEN = "exception.forbidden";
  public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

  public static final String INVALID_SOME_THING_FIELD = "invalid.general";
  public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
  public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
  public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
  public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
  public static final String INVALID_FORMAT_EMAIL = "invalid.email-format";
  public static final String INVALID_DATE = "invalid.date-format";
  public static final String INVALID_DATE_FEATURE = "invalid.date-future";
  public static final String INVALID_DATETIME = "invalid.datetime-format";

  public static class Auth {
    public static final String ERR_INCORRECT_USERNAME = "exception.auth.incorrect.username";
    public static final String ERR_INCORRECT_PASSWORD = "exception.auth.incorrect.password";
    public static final String ERR_NOT_MATCH_PASSWORD = "exception.auth.not.match.password";
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
    public static final String ERR_SYSTEM_PROCESS = "auth.register.system_process_error";
    public static final String ERR_SESSION_EXPIRED = "auth.register.session_expired";

  }

  public static class User {
    public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
    public static final String ERR_NOT_FOUND_ID = "exception.user.not.found.id";
    public static final String ERR_DUPLICATE_EMAIL = "exception.user.duplicate.email";
    public static final String ERR_DUPLICATE_PHONE = "exception.user.duplicate.phone";
    public static final String ERR_INVALID_AVATAR = "exception.user.invalid.avatar";
    public static final String ERR_USER_ALREADY_DELETED = "exception.user.already.deleted";
  }

  public static class Subject {
    public static final String ERR_NOT_FOUND_ID = "exception.subject.not.found.id";
    public static final String ERR_DUPLICATE_NAME = "exception.subject.duplicate.name";
    public static final String ERR_INVALID_TYPE = "exception.subject.invalid.type";
    public static final String ERR_NOT_BELONG_TO_USER = "exception.subject.not.belong.to.user";
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

  public static class Location {
    public static final String ERR_INACTIVE_STATUS = "exception.location.inactive_status";
    public static final String ERR_BUDDY_INACTIVE = "exception.location.buddy.inactive";
    public static final String ERR_INVALID_RADIUS = "exception.location.invalid.radius";
    public static final String ERR_INVALID_COORDINATES = "exception.location.invalid.coordinates";
  }

  public static class File {
    public static final String ERR_FILE_EMPTY = "exception.file.empty";
    public static final String ERR_FILE_SIZE_EXCEED = "exception.file.size_exceed";
    public static final String ERR_FILE_TYPE_NOT_ALLOWED = "exception.file.type_not_allowed";
    public static final String ERR_FILE_NAME_INVALID = "exception.file.name_invalid";
    public static final String ERR_FILE_UPLOAD_FAILED = "exception.file.upload_failed";
  }

  public static class Chat {
    public static final String ERR_BLOCKED = "exception.chat.user_blocked";
    public static final String ERR_CANNOT_BLOCK_YOURSELF = "exception.chat.cannot_block_yourself";
    public static final String ERR_MESSAGE_NOT_FOUND = "exception.chat.message_not_found";
    public static final String ERR_NOT_AUTHOR = "exception.chat.not_author";
    public static final String ERR_CANNOT_RECALL_AFTER_30_MINS = "exception.chat.cannot_recall_after_30_mins";
    public static final String ERR_CONVERSATION_NOT_FOUND = "exception.chat.conversation_not_found";
    public static final String ERR_NOT_IN_CONVERSATION = "exception.chat.not_in_conversation";
    public static final String ERR_MESSAGE_ALREADY_RECALLED = "exception.chat.message_already_recalled";
  public static class Chat {
    public static final String ERR_CONVERSATION_NOT_FOUND = "exception.chat.conversation.not.found";
    public static final String ERR_NOT_MEMBER = "exception.chat.not.member";
    public static final String ERR_USER_BLOCKED = "exception.chat.user.blocked";
    public static final String ERR_GROUP_FULL = "exception.chat.group.full";
    public static final String ERR_INVALID_FILE = "exception.chat.invalid.file";
    public static final String ERR_FILE_TOO_LARGE = "exception.chat.file.too.large";
    public static final String ERR_ALREADY_BLOCKED = "exception.chat.already.blocked";
    public static final String ERR_MESSAGE_NOT_FOUND = "exception.chat.message.not.found";
    public static final String ERR_NOT_SENDER = "exception.chat.not.sender";
    public static final String ERR_RECALL_TIMEOUT = "exception.chat.recall.timeout";
    public static final String ERR_ALREADY_LEFT = "exception.chat.already.left";
    public static final String ERR_SELF_CHAT = "exception.chat.self.chat";
  }


}
