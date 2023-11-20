package com.example.logging.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor(force = true)
@Getter
@Setter
@Accessors(chain = true)
@ToString
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Column(name = "name")
    private String name;

    public Role(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Role role)) return false;

        return getName().equals(role.getName());
    }

    @Override
    public int hashCode() {

        return getName().hashCode();
    }
}
