package org.exercise.core.enums;

import lombok.Getter;

@Getter
public enum OperationType {

    ADDITION(0, "addition"),
    SUBTRACTION(1, "subtraction"),
    MULTIPLICATION(2, "multiplication"),
    DIVISION(3, "division"),
    SQUARE_ROOT(4, "square_root"),
    RANDOM_STRING(5, "random_string");

    private final Integer id;
    private final String name;

    OperationType(int i, String name) {
        this.name = name;
        this.id = i;
    }

}
