package org.hit.chiikaiwabe.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTitle {
    TAN_BINH("Tân Binh", "*", 0, 49),
    TAN_TINH("Tân Tinh", "**", 50, 149),
    SAO_SANG("Sao Sáng", "***", 150, 349),
    KIM_CUONG("Kim Cương", "<>", 350, 699),
    LAO_LANG("Lão Làng", "##", 700, 1199),
    HUYEN_THOAI("Huyền Thoại", "@@", 1200, Integer.MAX_VALUE);

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
        return TAN_BINH;
    }

    public UserTitle next() {
        int nextOrdinal = this.ordinal() + 1;
        UserTitle[] values = values();
        return nextOrdinal < values.length ? values[nextOrdinal] : null;
    }
}
