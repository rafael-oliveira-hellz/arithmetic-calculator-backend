package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.exercise.core.enums.OperationType;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_operations")
public class Operation {

    @Id
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer id;
    private OperationType type;
    private Integer cost;

}
