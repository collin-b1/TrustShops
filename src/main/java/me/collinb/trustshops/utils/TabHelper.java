package me.collinb.trustshops.utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabHelper {

    public static List<String> getTabCompleteItems(@NotNull String[] strings, @NotNull World world) {
        String partialItem = strings[strings.length - 1].toUpperCase().replace("MINECRAFT:", "");
        return Arrays.stream(Material.values())
                .filter(material -> material.isEnabledByFeature(world))
                .filter(ItemHelper::isValidShopItem)
                .map(material -> "minecraft:" + material.toString().toLowerCase())
                .filter(name -> name.startsWith("minecraft:" + partialItem.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}
