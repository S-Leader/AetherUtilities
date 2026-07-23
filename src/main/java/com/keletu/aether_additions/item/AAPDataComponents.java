package com.keletu.aether_additions.item;

import com.keletu.aether_additions.AetherAdditions;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AAPDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AetherAdditions.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> MOA =
            COMPONENTS.register("moa", () -> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());

    private AAPDataComponents() {
    }
}
