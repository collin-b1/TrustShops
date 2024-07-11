package me.collinb.trustshops.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
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

public class Shop implements Comparable<Shop> {
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

    public Shop(@NotNull OfflinePlayer shopOwner, @NotNull Location shopLocation, @NotNull Material containerItem, int containerAmount, @NotNull Material playerItem, int playerAmount) {
        this.shopOwner = shopOwner;
        this.shopLocation = shopLocation;
        this.containerItem = containerItem;
        this.containerAmount = containerAmount;
        this.playerItem = playerItem;
        this.playerAmount = playerAmount;
        updateStock();
    }

    public Shop(@NotNull OfflinePlayer shopOwner, @NotNull Material containerItem, int containerAmount, @NotNull Material playerItem, int playerAmount) {
        this.shopOwner = shopOwner;
        this.containerItem = containerItem;
        this.containerAmount = containerAmount;
        this.playerItem = playerItem;
        this.playerAmount = playerAmount;
        this.stock = 0;
    }

    /**
     * Setter for shop location, used when the player has selected a container in a command such as /tsinfo.
     * @param location Location of the shop
     */
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

    /**
     * Get a single shop's display line for shop commands
     * @param showStock Should stock tag be shown? Looks like: <b>[Stock: n]</b>
     * @param showLocation Should location be shown? Looks like: <b>Dimension (x, y, z)</b>
     * @return TextComponent for the line(s) displayed for a single shop at a location.
     */
    public TextComponent getDisplayLine(boolean showStock, boolean showLocation) {
        TextComponent textComponent = Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text(Objects.requireNonNull(getShopOwner().getName())).color(NamedTextColor.GRAY))
                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getContainerAmount())
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                )
                .append(Component.text("x").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getContainerItem().name())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showItem(
                                getContainerItem().key(),
                                getContainerAmount()
                        ))
                )
                .append(Component.text(" for ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getPlayerAmount())
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                )
                .append(Component.text("x").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(getPlayerItem().name())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showItem(
                                getPlayerItem().key(),
                                getPlayerAmount()
                        ))
                );
        if (showStock || showLocation) {
            textComponent = textComponent.append(Component.text("\n╚ ").color(NamedTextColor.DARK_GRAY));
            if (showStock) {
                textComponent = textComponent.append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                        .append(Component.text("Stock: ").color(NamedTextColor.DARK_GRAY))
                        .append(Component.text(getStock()).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD))
                        .append(Component.text("] "));
            }
            if (showLocation) {
                String dimension = switch (getShopLocation().getWorld().getEnvironment()) {
                    case NORMAL -> "Overworld";
                    case NETHER -> "Nether";
                    case THE_END -> "The End";
                    default -> getShopLocation().getWorld().getName();
                };
                textComponent = textComponent.append(Component.text(String.format("%s (%d, %d, %d)",
                        dimension,
                        getShopLocation().getBlockX(),
                        getShopLocation().getBlockY(),
                        getShopLocation().getBlockZ()
                        )).color(NamedTextColor.DARK_GRAY)
                );
            }
        }


        return textComponent;
    }

    /**
     * Retrieves the shop's stock and stores it in stock variable.
     */
    public void updateStock() {
        // @TODO Move all of this to constructor?
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

    /**
     * Get the total stock for sale. This is different from just the total amount in the container because it only
     * includes the amount that can be sold, and ignores excess items.
     * @return Stock * Sale Amount
     */
    public int getTotalForSale() {
        return stock * containerAmount;
    }

    @Override
    public int compareTo(@NotNull Shop o) {
        return Integer.compare(o.getTotalForSale(), getTotalForSale());
    }

    // Only one shop selling an item X for an item Y can exist at location Z
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        Shop shop = (Shop)o;
        return shopLocation.equals(shop.getShopLocation())
                && containerItem.equals(shop.getContainerItem())
                && playerItem.equals(shop.getPlayerItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopLocation, containerItem, playerItem);
    }

    @Override
    public String toString() {
        return "ContainerShop{" +
                "shopLocation=" + shopLocation +
                ", playerItem='" + playerItem + '\'' +
                ", containerItem='" + containerItem + '\'' +
                '}';
    }
}
