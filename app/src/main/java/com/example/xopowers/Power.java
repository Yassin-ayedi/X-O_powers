package com.example.xopowers;

/**
 * Enum representing all available powers in the game.
 * Each power has a name, description, emoji icon, and color.
 */
public enum Power {

    NONE("No Power", "Just place your mark normally.", "✖", "#AAAAAA"),

    BOMB("💣 Bomb", "Destroys an opponent's mark on an adjacent cell.", "💣", "#FF5722"),

    SHIELD("🛡 Shield", "Protects your mark from being destroyed for 2 turns.", "🛡", "#2196F3"),

    STEAL("🃏 Steal", "Convert any one opponent mark into yours (if not shielded).", "🃏", "#9C27B0"),

    DOUBLE("⚡ Double", "Place TWO marks this turn instead of one.", "⚡", "#FFC107"),

    CHAIN("⛓ Chain", "Block an empty cell — opponent cannot place there for a full round.", "⛓", "#FF9800"),

    MIRROR("🪞 Mirror", "Copy your opponent's last placed mark position as your own.", "🪞", "#4CAF50"),

    WILDCARD("🃏 Wild", "Place your mark anywhere, even on a frozen cell.", "🃏", "#FF9800");

    private final String displayName;
    private final String description;
    private final String icon;
    private final String color;

    Power(String displayName, String description, String icon, String color) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}