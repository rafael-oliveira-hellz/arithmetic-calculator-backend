package org.exercise.core.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_id", referencedColumnName = "id")
    private Balance balance;

    public User(String username, String password, String email, UUID cognitoUserId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.id = cognitoUserId;
        this.active = true;
        this.balance.setAmount(Integer.valueOf(System.getenv("AMOUNT")));
    }

    public User(UUID userId, String username, String mail, boolean active, Balance balance) {
        this.id = userId;
        this.username = username;
        this.email = mail;
        this.active = active;
        this.balance = balance;
    }
}
