package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_balances")
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @OneToOne(mappedBy = "balance", cascade = CascadeType.ALL)
    private User user;
    private Integer amount;

}
