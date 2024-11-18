package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @OneToOne(targetEntity = Operation.class)
    private Operation operation;
    @OneToOne(targetEntity = User.class)
    private User user;
    private Integer amount;
    private Integer userBalance;
    private String operationResponse;
    private ZonedDateTime date;
    private Boolean deleted;

    public Record(Operation operation, User user, Integer amount, Integer userBalance, String operationResponse) {
        this.operation = operation;
        this.user = user;
        this.amount = amount;
        this.userBalance = userBalance;
        this.operationResponse = operationResponse;
        this.deleted = false;
    }
}
