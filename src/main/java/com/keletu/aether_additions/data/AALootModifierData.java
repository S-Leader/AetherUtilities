package com.keletu.aether_additions.data;

import com.aetherteam.aether.loot.AetherLoot;
import com.aetherteam.nitrogen.loot.modifiers.AddDungeonLootModifier;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AAPItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AALootModifierData extends GlobalLootModifierProvider {
    public AALootModifierData(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, AetherAdditions.MODID);
    }

    @Override
    protected void start() {
        this.add("vampire_crossbow", new AddDungeonLootModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(AetherLoot.GOLD_DUNGEON_REWARD.location()).build()
                },
                List.of(WeightedEntry.wrap(new ItemStack(AAPItems.VAMPIRE_CROSSBOW.get()), 12)),
                UniformInt.of(1, 1))
        );
    }
}