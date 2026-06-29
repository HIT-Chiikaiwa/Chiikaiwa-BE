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
    public static final String FORGOT_PASSWORD_VERIFY_SUCCESS = "auth.otp.verify_success"; // Bạn có thể dùng chung nếu nội dung giống nhau

    public static final String PASSWORD_UPDATED = "Password updated successfully";
    public static final String USER_DELETED = "User deleted successfully";
    public static final String SUBJECT_DELETED = "Subject deleted successfully";

    public static final String LOCATION_UPDATED = "location.update_success";

    public static class CleanupLocation{
        public static final String PROCESSING = "location.cleanup.processing";
        public static final String SUCCESS = "location.cleanup.success";
        public static final String TOTAL_COUNT_SUCCESS = "location.cleanup.total-count-success";
    }
}
