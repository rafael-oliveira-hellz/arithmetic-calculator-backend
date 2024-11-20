package org.exercise.domain.enums;

import lombok.Getter;

@Getter
public enum OperationType {

    ADDITION("addition"),
    SUBTRACTION("subtraction"),
    MULTIPLICATION("multiplication"),
    DIVISION("division"),
    SQUARE_ROOT("square_root"),
    RANDOM_STRING("random_string");

    private final String name;

    OperationType(String name) {
        this.name = name;
    }

}
