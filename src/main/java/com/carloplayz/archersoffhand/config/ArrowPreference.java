package com.carloplayz.archersoffhand.config;

public class ArrowPreference {
    public String itemId;
    public String displayName; // User-friendly name for display
    public String category;    // e.g., "Tipped", "Special", etc.
    
    public ArrowPreference() {}
    
    public ArrowPreference(String itemId, String displayName, String category) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.category = category;
    }
}