package org.myblog.users.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.myblog.users.appenum.RoleEnum;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class RoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleEnum name;

    public RoleModel(RoleEnum name) {
        this.name = name;
    }
}