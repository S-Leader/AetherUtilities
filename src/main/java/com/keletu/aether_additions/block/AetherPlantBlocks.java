package com.keletu.aether_additions.block;

import com.keletu.aether_additions.AetherAdditions;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Plant block registration. BEAN_VINE intentionally has no BlockItem registration.
 */
public final class AetherPlantBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AetherAdditions.MODID);

    public static final DeferredBlock<BeanVineBlock> BEAN_VINE = BLOCKS.register("bean_vine",
            () -> new BeanVineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noOcclusion()
                    .strength(0.2F)
                    .sound(SoundType.VINE)
                    .pushReaction(PushReaction.DESTROY)));

    private AetherPlantBlocks() {
    }
}
