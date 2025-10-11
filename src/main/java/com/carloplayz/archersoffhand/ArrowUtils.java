package com.carloplayz.archersoffhand;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ArrowUtils {
    
    public static class ArrowType {
        public final String itemId;
        public final String displayName;
        public final String category;
        
        public ArrowType(String itemId, String displayName, String category) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.category = category;
        }
    }
    
    /**
     * Get all available arrow types in the game
     */
    public static List<ArrowType> getAllArrowTypes() {
        List<ArrowType> arrowTypes = new ArrayList<>();
        
        // Iterate through all items in the registry
        for (Item item : Registries.ITEM) {
            String itemId = Registries.ITEM.getId(item).toString();
            
            // Check if it's an arrow type
            if (item instanceof net.minecraft.item.ArrowItem || itemId.contains("arrow")) {
                String displayName = item.getName().getString();
                String category = determineCategory(itemId);
                
                arrowTypes.add(new ArrowType(itemId, displayName, category));
            }
        }
        
        return arrowTypes;
    }
    
    /**
     * Get common arrow types that players would typically want to configure
     */
    public static List<ArrowType> getCommonArrowTypes() {
        List<ArrowType> commonTypes = new ArrayList<>();
        
        // Add vanilla arrow types
        commonTypes.add(new ArrowType("minecraft:arrow", "Arrow", "Basic"));
        commonTypes.add(new ArrowType("minecraft:spectral_arrow", "Spectral Arrow", "Special"));
        commonTypes.add(new ArrowType("minecraft:tipped_arrow", "Tipped Arrow", "Tipped"));
        
        // Note: Additional arrow types from other mods would also be detected by getAllArrowTypes()
        
        return commonTypes;
    }
    
    /**
     * Determine the category for an arrow type based on its ID
     */
    private static String determineCategory(String itemId) {
        if (itemId.contains("spectral")) {
            return "Special";
        } else if (itemId.contains("tipped")) {
            return "Tipped";
        } else if (itemId.contains("arrow")) {
            return "Basic";
        } else {
            return "Other";
        }
    }
    
    /**
     * Find arrow types that match a search query (case-insensitive)
     */
    public static List<ArrowType> searchArrowTypes(String query) {
        List<ArrowType> allTypes = getAllArrowTypes();
        List<ArrowType> results = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            return allTypes;
        }
        
        String lowerQuery = query.toLowerCase();
        
        for (ArrowType type : allTypes) {
            if (type.displayName.toLowerCase().contains(lowerQuery) || 
                type.itemId.toLowerCase().contains(lowerQuery) ||
                type.category.toLowerCase().contains(lowerQuery)) {
                results.add(type);
            }
        }
        
        return results;
    }
}