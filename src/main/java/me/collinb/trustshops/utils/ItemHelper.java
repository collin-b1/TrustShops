package me.collinb.trustshops.utils;

import me.collinb.trustshops.TrustShops;
import org.bukkit.Material;

public class ItemHelper {
    public static boolean isValidShopItem(Material material) {
        return material != null
                && material.isItem()
                && !TrustShops.BANNED_ITEMS.contains(material)
                && !material.isLegacy();
    }
}
