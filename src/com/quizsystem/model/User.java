package com.quizsystem.model;

/**
 * ============================================================
 * User — Represents a quiz participant.
 * ============================================================
 * 
 * KEY DESIGN DECISIONS:
 * 
 * 1. equals() & hashCode() OVERRIDE:
 *    We use User objects as keys in Map<User, Score>. For HashMap to
 *    work correctly, two User objects with the same id must be
 *    considered "equal" and produce the same hashCode.
 * 
 *    Without these overrides:
 *      User u1 = new User(1, "Alice");
 *      User u2 = new User(1, "Alice");
 *      u1.equals(u2) → false  (uses Object.equals → reference check)
 *      map.get(u2) → null     (can't find the entry keyed by u1)
 * 
 *    With our overrides:
 *      u1.equals(u2) → true   (same id)
 *      map.get(u2) → Score    (works correctly!)
 * 
 * 2. IMMUTABILITY:
 *    Fields are final — once a User is created, their id and username
 *    cannot change. This makes User safe to use as a Map key.
 *    (Mutable Map keys are a common source of bugs.)
 */
public class User {

    // ── Final fields (immutable) ────────────────────────────────
    private final int id;
    private final String username;

    // ── Constructor ─────────────────────────────────────────────
    /**
     * Creates a new User.
     *
     * @param id       unique identifier (from database or auto-generated)
     * @param username the display name of the user
     */
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // ── Getters ─────────────────────────────────────────────────

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // ── equals & hashCode (critical for Map<User, Score>) ───────

    /**
     * Two users are equal if they have the same id.
     * This follows the database convention where id is the primary key.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    /**
     * Must be consistent with equals():
     * if a.equals(b), then a.hashCode() == b.hashCode().
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s'}", id, username);
    }
}
