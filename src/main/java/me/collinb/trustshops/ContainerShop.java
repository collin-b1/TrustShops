package me.collinb.trustshops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ContainerShop implements Comparable<ContainerShop> {
    // Owner of the shop
    private final OfflinePlayer shopOwner;

    // Location of the container
    private Location shopLocation;

    // Item being sold in the container
    private final Material containerItem;

    // Item being bought by the container
    private final Material playerItem;

    // Amount being sold in the container
    private final int containerAmount;

    // Amount being bought by the container
    private final int playerAmount;

    // Amount of sold stacks remaining
    private int stock;

    public ContainerShop(@NotNull OfflinePlayer shopOwner, @NotNull Location shopLocation, @NotNull Material containerItem, int containerAmount, @NotNull Material playerItem, int playerAmount) {
        this.shopOwner = shopOwner;
        this.shopLocation = shopLocation;
        this.containerItem = containerItem;
        this.containerAmount = containerAmount;
        this.playerItem = playerItem;
        this.playerAmount = playerAmount;
        updateStock();
    }

    public ContainerShop(@NotNull OfflinePlayer shopOwner, @NotNull Material containerItem, int containerAmount, @NotNull Material playerItem, int playerAmount) {
        this.shopOwner = shopOwner;
        this.containerItem = containerItem;
        this.containerAmount = containerAmount;
        this.playerItem = playerItem;
        this.playerAmount = playerAmount;
        this.stock = 0;
    }

    public void setShopLocation(@NotNull Location location) {
        this.shopLocation = location;
    }

    public Location getShopLocation() {
        return this.shopLocation;
    }

    public OfflinePlayer getShopOwner() {
        return this.shopOwner;
    }

    public Material getContainerItem() {
        return this.containerItem;
    }

    public Material getPlayerItem() {
        return this.playerItem;
    }

    public int getContainerAmount() {
        return this.containerAmount;
    }

    public int getPlayerAmount() {
        return this.playerAmount;
    }

    public int getStock() {
        return this.stock;
    }

    public TextComponent getDisplayLine() {
        return Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text(Objects.requireNonNull(getShopOwner().getName())).color(NamedTextColor.GRAY))
                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getContainerAmount()).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text("x").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getContainerItem().name()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" for ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getPlayerAmount()).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text("x").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getPlayerItem().name()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" [").color(NamedTextColor.DARK_GRAY))
                .append(Component.text("Stock: ").color(NamedTextColor.GRAY))
                .append(Component.text(getStock()).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(String.format("] (%d, %d)", getShopLocation().getBlockX(), getShopLocation().getBlockZ()))
                        .color(NamedTextColor.DARK_GRAY));
    }

    public void updateStock() {
        if (shopLocation == null) {
            return;
        }
        Block block = shopLocation.getBlock();

        if (!(block.getState() instanceof Container container)) {
            return;
        }
        int totalSold = 0;
        for (ItemStack itemStack : container.getInventory()) {
            if (itemStack != null && itemStack.getType().equals(containerItem)) {
                totalSold += itemStack.getAmount();
            }
        }
        totalSold /= containerAmount;
        stock = totalSold;
    }

    public int getTotalForSale() {
        return stock * containerAmount;
    }

    @Override
    public int compareTo(@NotNull ContainerShop o) {
        return o.getTotalForSale() - getTotalForSale();
    }
}
