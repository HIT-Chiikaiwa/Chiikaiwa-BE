package org.hit.chiikaiwabe.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointAction {
    BOOKING_COMPLETED(20, "Hoàn thành lịch hẹn"),
    BOOKING_REJECTED(-5, "Lịch hẹn bị từ chối"),
    BOOKING_CANCELLED(-10, "Hủy lịch hẹn"),
    BOOKING_EXPIRED(-5, "Lịch hẹn hết hạn"),
    BOOKING_RATED(5, "Đánh giá partner"),
    RATING_5_STAR_RECEIVED(10, "Nhận đánh giá 5 sao"),
    RATING_1_STAR_RECEIVED(-3, "Nhận đánh giá 1 sao"),
    FRIENDSHIP_ACCEPTED(3, "Kết bạn thành công"),
    DAILY_FIRST_MESSAGE(1, "Tin nhắn đầu tiên trong ngày");

    private final int points;
    private final String description;
}
