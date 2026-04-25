package com.quizsystem.model;

/**
 * ============================================================
 * Category Enum — Represents quiz question categories.
 * ============================================================
 * 
 * WHY AN ENUM?
 * - Enums are type-safe constants. Unlike plain Strings, the compiler
 *   will catch typos like "SCINCE" at compile time.
 * - Each enum constant can carry data (here, a human-readable display name).
 * - Enums are singletons — only one instance of each constant exists.
 * 
 * DESIGN NOTE:
 * Adding a new category is as simple as adding a new constant here.
 * All services, repositories, and UI code will pick it up automatically
 * because they operate on the Category type, not on magic strings.
 */
public enum Category {

    SCIENCE("Science"),
    HISTORY("History"),
    MATH("Mathematics"),
    PROGRAMMING("Programming"),
    GENERAL("General Knowledge");

    // ── Instance field ──────────────────────────────────────────
    private final String displayName;

    // ── Constructor (always private in enums) ───────────────────
    Category(String displayName) {
        this.displayName = displayName;
    }

    // ── Getter ──────────────────────────────────────────────────
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Safely converts a string to a Category, returning GENERAL as fallback.
     * Useful when reading from files or user input where typos may occur.
     *
     * @param text the category name to parse
     * @return the matching Category, or GENERAL if no match
     */
    public static Category fromString(String text) {
        for (Category cat : Category.values()) {
            if (cat.name().equalsIgnoreCase(text) 
                || cat.displayName.equalsIgnoreCase(text)) {
                return cat;
            }
        }
        return GENERAL; // safe default
    }
}
