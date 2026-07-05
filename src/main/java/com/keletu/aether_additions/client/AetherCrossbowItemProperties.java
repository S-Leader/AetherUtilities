package com.keletu.aether_additions.client;

import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AetherCrossbowItem;
import com.keletu.aether_additions.item.AetherCrossbowItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

public final class AetherCrossbowItemProperties {
    private AetherCrossbowItemProperties() {
    }

    public static void register() {
        register(AetherCrossbowItems.SKYROOT_CROSSBOW.get());
        register(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get());
        register(AetherCrossbowItems.ZANITE_CROSSBOW.get());
        register(AetherCrossbowItems.GRAVITITE_CROSSBOW.get());
        register(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
    }

    private static void register(AetherCrossbowItem item) {
        ItemProperties.register(item, vanilla("pull"), (stack, level, entity, seed) -> {
            if (entity == null || CrossbowItem.isCharged(stack) || entity.getUseItem() != stack) {
                return 0.0F;
            }
            return getPull(stack, entity, item);
        });

        ItemProperties.register(item, vanilla("pulling"), (stack, level, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
        });

        ItemProperties.register(item, vanilla("charged"), (stack, level, entity, seed) -> {
            return CrossbowItem.isCharged(stack) ? 1.0F : 0.0F;
        });

        ItemProperties.register(item, vanilla("firework"), (stack, level, entity, seed) -> {
            ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
            return chargedProjectiles != null && chargedProjectiles.contains(net.minecraft.world.item.Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
        });

        // Old Aether-compatible alias. Useful if your existing model json used "ready".
        ItemProperties.register(item, mod("ready"), (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
    }

    private static float getPull(ItemStack stack, LivingEntity entity, AetherCrossbowItem item) {
        int usedTicks = item.getUseDuration(stack, entity) - entity.getUseItemRemainingTicks();
        return Math.min(usedTicks / (float) item.getAetherChargeDuration(stack, entity), 1.0F);
    }

    private static ResourceLocation vanilla(String name) {
        return ResourceLocation.withDefaultNamespace(name);
    }

    private static ResourceLocation mod(String name) {
        return ResourceLocation.fromNamespaceAndPath(AetherAdditions.MODID, name);
    }
}
