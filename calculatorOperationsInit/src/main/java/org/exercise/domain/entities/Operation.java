package org.exercise.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.exercise.domain.enums.OperationType;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_operations")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private OperationType type;

    @Column(nullable = false)
    private Integer cost;

    public Operation(OperationType type, Integer cost) {
        this.type = type;
        this.cost = cost;
    }
}

