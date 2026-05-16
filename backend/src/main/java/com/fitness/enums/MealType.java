package com.fitness.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MealType {
    BREAKFAST(1, "早餐"),
    LUNCH(2, "午餐"),
    DINNER(3, "晚餐"),
    SNACK(4, "加餐");

    private final int code;
    private final String desc;

    public static MealType of(int code) {
        for (MealType m : values()) {
            if (m.code == code) return m;
        }
        return BREAKFAST;
    }
}
