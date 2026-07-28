package org.hit.chiikaiwabe.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTitle {
    SIEU_TAN_BINH("Siêu Tân Binh", "*", 0, 199),
    TAN_BINH_KY_CUU("Tân Binh Kỳ Cựu", "**", 200, 599),
    CO_CONG_MAI_SAT("Có Công Mài Sắt", "***", 600, 799),
    CONG_SU_SIEU_DANG("Cộng Sự Siêu Đẳng", "<>", 800, 999),
    LAO_LANG("Lão Làng", "##", 1000, Integer.MAX_VALUE);

    private final String displayName;
    private final String icon;
    private final int minExp;
    private final int maxExp;

    public static UserTitle fromExp(long exp) {
        for (UserTitle title : values()) {
            if (exp >= title.minExp && exp <= title.maxExp) {
                return title;
            }
        }
        return SIEU_TAN_BINH;
    }

    public UserTitle next() {
        int nextOrdinal = this.ordinal() + 1;
        UserTitle[] values = values();
        return nextOrdinal < values.length ? values[nextOrdinal] : null;
    }
}
