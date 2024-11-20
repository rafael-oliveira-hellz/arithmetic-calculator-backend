package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.exercise.core.enums.OperationType;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_operations")
public class Operation {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private Integer cost;
}
