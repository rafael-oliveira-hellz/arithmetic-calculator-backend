package org.exercise.core.enums;

import lombok.Getter;

@Getter
public enum OperationType {

    ADDITION(0),
    SUBTRACTION(1),
    MULTIPLICATION(2),
    DIVISION(3),
    SQUARE_ROOT(4),
    RANDOM_STRING(5);

    private final Integer id;

    OperationType(int i) {
        this.id = i;
    }

}
