package com.example.logging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(force = true)
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "users")
public class User implements UserDetails, Cloneable {

    @JsonProperty(value = "username")
    @Id
    @Column(name = "username")
    private String username;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false)
    private String password;

    @JsonProperty(value = "email")
    @Column(name = "email")
    private String email;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JsonProperty(value = "forename")
    @Column(name = "forename", nullable = false)
    private String forename;

    @JsonProperty(value = "surname")
    @Column(name = "surname", nullable = false)
    private String surname;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "username", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_name", nullable = false))
    private List<Role> roles;

    @JsonIgnore
    @Column(name = "account_non_expired", nullable = false)
    @Getter(value = AccessLevel.NONE)
    private boolean accountNonExpired;

    @JsonIgnore
    @Column(name = "account_non_locked", nullable = false)
    @Getter(value = AccessLevel.NONE)
    private boolean accountNonLocked;

    @JsonIgnore
    @Column(name = "credentials_non_expired", nullable = false)
    @Getter(value = AccessLevel.NONE)
    private boolean credentialsNonExpired;

    @JsonIgnore
    @Column(name = "enabled", nullable = false)
    @Getter(value = AccessLevel.NONE)
    private boolean enabled;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (roles == null) {
            throw new RuntimeException("User roles are null. Cannot create List of GrantedAuthority");
        }

        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
    }

    @Override
    public boolean isAccountNonExpired() {

        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {

        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {

        return enabled;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return getUsername().equals(user.getUsername());
    }

    @Override
    public int hashCode() {

        return getUsername().hashCode();
    }

    @Override
    public User clone() {

        try {
            return ((User) super.clone())
                    .setRoles(this.roles == null ? null : this.roles.stream().toList());
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
