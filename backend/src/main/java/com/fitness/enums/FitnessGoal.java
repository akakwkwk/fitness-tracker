package com.fitness.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FitnessGoal {
    BULK(1, "增肌"),
    CUT(2, "减脂"),
    MAINTAIN(3, "维持"),
    STRENGTH(4, "力量提升");

    private final int code;
    private final String desc;

    public static FitnessGoal of(int code) {
        for (FitnessGoal g : values()) {
            if (g.code == code) return g;
        }
        return MAINTAIN;
    }
}
