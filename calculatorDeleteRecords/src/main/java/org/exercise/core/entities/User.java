package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_users")
public class User {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private Boolean active;
    @OneToOne(cascade = CascadeType.ALL)
    private Balance balance;

}
