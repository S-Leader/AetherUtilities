package com.keletu.aether_additions.event;

import com.aetherteam.aether.block.AetherBlocks;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AAPItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Adds Magic Beans to Aether grass drops without replacing The Aether's loot table.
 */
@EventBusSubscriber(modid = AetherAdditions.MODID)
public final class AetherPlantDropEvents {
    private static final int MAGIC_BEAN_CHANCE = 20;

    private AetherPlantDropEvents() {
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player)) {
            return;
        }
        if (!event.getLevel().getBlockState(event.getPos().below()).is(AetherBlocks.AETHER_GRASS_BLOCK.get()) && !event.getLevel().getBlockState(event.getPos().below()).is(AetherBlocks.ENCHANTED_AETHER_GRASS_BLOCK.get())) {
            return;
        }

        if (!event.getState().is(Blocks.SHORT_GRASS) && !event.getState().is(Blocks.TALL_GRASS)) {
            return;
        }

        if (event.getLevel().getRandom().nextInt(MAGIC_BEAN_CHANCE) != 0) {
            return;
        }

        ItemEntity bean = new ItemEntity(
                event.getLevel(),
                event.getPos().getX() + 0.5D,
                event.getPos().getY() + 0.5D,
                event.getPos().getZ() + 0.5D,
                new ItemStack(AAPItems.MAGIC_BEAN.get())
        );
        event.getDrops().add(bean);
    }
}
