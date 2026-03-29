package com.hospital.appointment.domain;

import java.util.Objects;

/**
 * Abstract base for people in the system. Encapsulates identity; subclasses supply role-specific behaviour.
 */
public abstract class User {

    private final long id;
    private String fullName;
    private String email;

    protected User(long id, String fullName, String email) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        this.id = id;
        setFullName(fullName);
        setEmail(email);
    }

    public final long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName, "fullName").trim();
        if (this.fullName.isEmpty()) {
            throw new IllegalArgumentException("fullName cannot be blank");
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "email").trim();
        if (this.email.isEmpty()) {
            throw new IllegalArgumentException("email cannot be blank");
        }
    }

    /** Polymorphic hook: each role describes itself for UI/logging. */
    public abstract String getRoleLabel();

    /** Polymorphic contact line for notifications (polymorphism demo). */
    public abstract String getContactSummary();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
