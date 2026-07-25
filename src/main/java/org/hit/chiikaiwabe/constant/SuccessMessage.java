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

    public static class CleanupLocation{
        public static final String PROCESSING = "location.cleanup.processing";
        public static final String SUCCESS = "location.cleanup.success";
        public static final String TOTAL_COUNT_SUCCESS = "location.cleanup.total-count-success";
    }


    public static class Chat {
        public static final String MESSAGE_SENT = "success.chat.message.sent";
        public static final String GROUP_CREATED = "success.chat.group.created";
        public static final String MEMBER_ADDED = "success.chat.member.added";
        public static final String MEMBER_REMOVED = "success.chat.member.removed";
        public static final String USER_BLOCKED_SUCCESS = "success.chat.user.blocked";
        public static final String USER_UNBLOCKED_SUCCESS = "success.chat.user.unblocked";
        public static final String REPORT_SUBMITTED_SUCCESS = "success.chat.report.submitted";
        public static final String FILE_UPLOADED = "success.chat.file.uploaded";
        public static final String DEVICE_REGISTERED = "success.chat.device.registered";
        public static final String MESSAGE_DELETED_SUCCESS = "success.chat.message.deleted";
        public static final String MESSAGE_RECALLED_SUCCESS = "success.chat.message.recalled";

        public static final String MESSAGE_PINNED = "success.chat.message.pinned";
        public static final String MESSAGE_UNPINNED = "success.chat.message.unpinned";
        public static final String MESSAGE_FORWARDED = "success.chat.message.forwarded";
        public static final String REACTION_ADDED = "success.chat.reaction.added";
        public static final String REACTION_REMOVED = "success.chat.reaction.removed";
        public static final String GROUP_DISSOLVED = "success.chat.group.dissolved";
        public static final String OWNERSHIP_TRANSFERRED = "success.chat.ownership.transferred";
    }

    public static class Device {
        public static final String REGISTER = "success.chat.device.registered";
        public static final String UNREGISTER = "success.chat.device.unregistered";
    }

    public static class Friendship {
        public static final String REQUEST_SENT = "success.friendship.request.sent";
        public static final String REQUEST_ACCEPTED = "success.friendship.request.accepted";
        public static final String REQUEST_REJECTED = "success.friendship.request.rejected";
        public static final String UNFRIENDED = "success.friendship.unfriended";
    }

    public static class Booking {
        public static final String PUSH_NEW_REQUEST_TITLE = "push.booking.new.request.title";
        public static final String PUSH_NEW_REQUEST_BODY = "push.booking.new.request.body";
        public static final String PUSH_ACCEPTED_TITLE = "push.booking.accepted.title";
        public static final String PUSH_ACCEPTED_BODY = "push.booking.accepted.body";
        public static final String PUSH_REJECTED_TITLE = "push.booking.rejected.title";
        public static final String PUSH_REJECTED_BODY = "push.booking.rejected.body";

        public static final String CREATED = "success.booking.created";
        public static final String ACCEPTED = "success.booking.accepted";
        public static final String REJECTED = "success.booking.rejected";
        public static final String CANCELLED = "success.booking.cancelled";
        public static final String COMPLETED = "success.booking.completed";
        public static final String RATED = "success.booking.rated";
    }

    public static class Leaderboard {
        public static final String POINTS_AWARDED = "success.leaderboard.points.awarded";
    }

}
