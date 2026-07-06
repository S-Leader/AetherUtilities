package com.keletu.aether_additions.data;

import com.aetherteam.aether.AetherTags;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AetherCrossbowItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class AATags extends ItemTagsProvider {
    public AATags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper helper) {
        super(output, registries, blockTags, AetherAdditions.MODID, helper);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(ItemTags.CROSSBOW_ENCHANTABLE).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.RANGED_WEAPON_TOOLS).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.TOOLS).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());

        this.tag(ItemTags.DURABILITY_ENCHANTABLE).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
        this.tag(ItemTags.VANISHING_ENCHANTABLE).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.ENCHANTABLES).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());

        this.tag(AetherTags.Items.TREATED_AS_AETHER_ITEM).add(AetherCrossbowItems.SKYROOT_CROSSBOW.get()).add(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()).add(AetherCrossbowItems.ZANITE_CROSSBOW.get()).add(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
        this.tag(AetherTags.Items.GOLD_DUNGEON_LOOT).add(AetherCrossbowItems.VAMPIRE_CROSSBOW.get());
    }
}