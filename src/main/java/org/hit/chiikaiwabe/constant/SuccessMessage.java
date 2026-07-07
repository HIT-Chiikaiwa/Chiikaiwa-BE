package org.hit.chiikaiwabe.constant;

public class SuccessMessage {
    public static final String LOGOUT_SUCCESS = "success.auth.logout";
    public static final String REGISTER_SUCCESS = "auth.register.success";
    public static final String SEND_OTP_SUCCESS = "auth.otp.send_success";
    public static final String VERIFY_OTP_SUCCESS = "auth.otp.verify_success";
    public static final String RESET_PASSWORD_SUCCESS = "auth.password.reset_success";
    public static final String REGISTER_SUCCESS_CHECK_EMAIL = "auth.register.success.check_email";
    public static final String VERIFY_REGISTER_SUCCESS = "auth.register.verify.success";
    public static final String FORGOT_PASSWORD_SEND_OTP_SUCCESS = "auth.forgot_password.send_otp_success";
    public static final String FORGOT_PASSWORD_VERIFY_SUCCESS = "auth.otp.verify_success";

    public static final String PASSWORD_UPDATED = "success.password.updated";
    public static final String USER_DELETED = "success.user.deleted";
    public static final String SUBJECT_DELETED = "success.subject.deleted";
    public static final String LOCATION_UPDATED = "success.location.updated";
    public static final String LOCATION_REMOVED = "success.location.removed";

    public static final String MESSAGE_SENT = "success.chat.message.sent";
    public static final String GROUP_CREATED = "success.chat.group.created";
    public static final String MEMBER_ADDED = "success.chat.member.added";
    public static final String MEMBER_REMOVED = "success.chat.member.removed";
    public static final String USER_BLOCKED = "success.chat.user.blocked";
    public static final String USER_UNBLOCKED = "success.chat.user.unblocked";
    public static final String REPORT_SUBMITTED = "success.chat.report.submitted";
    public static final String FILE_UPLOADED = "success.chat.file.uploaded";
    public static final String DEVICE_REGISTERED = "success.chat.device.registered";
    public static final String MESSAGE_DELETED = "success.chat.message.deleted";
    public static final String MESSAGE_RECALLED = "success.chat.message.recalled";


    public static class CleanupLocation{
        public static final String PROCESSING = "location.cleanup.processing";
        public static final String SUCCESS = "location.cleanup.success";
        public static final String TOTAL_COUNT_SUCCESS = "location.cleanup.total-count-success";
    }
}
