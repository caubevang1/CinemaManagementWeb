package com.cinemaweb.API.Cinema.Web.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    String ID;

    String username;
    String password;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Integer gender;
    String avatar;
    @Column(unique = true)
    String email;
    @Column(unique = true)
    String phoneNumber;
    Double point;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    Set<Role> roles;

    public void getBonus(double point) {
        this.point += point;
    }
}
