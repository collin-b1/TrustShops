package me.collinb.trustshops;

import me.collinb.trustshops.enums.ContainerShopModificationType;

public record ContainerShopPendingAction(ContainerShop shop, ContainerShopModificationType type) {
}
