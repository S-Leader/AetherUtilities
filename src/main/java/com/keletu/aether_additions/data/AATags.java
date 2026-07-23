package com.keletu.aether_additions.data;

import com.aetherteam.aether.AetherTags;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.item.AAPItems;
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
        this.tag(ItemTags.CROSSBOW_ENCHANTABLE).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.RANGED_WEAPON_TOOLS).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.TOOLS).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());

        this.tag(ItemTags.DURABILITY_ENCHANTABLE).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());
        this.tag(ItemTags.VANISHING_ENCHANTABLE).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());
        this.tag(Tags.Items.ENCHANTABLES).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get());

        this.tag(AetherTags.Items.TREATED_AS_AETHER_ITEM).add(AAPItems.SKYROOT_CROSSBOW.get()).add(AAPItems.HOLYSTONE_CROSSBOW.get()).add(AAPItems.ZANITE_CROSSBOW.get()).add(AAPItems.GRAVITITE_CROSSBOW.get()).add(AAPItems.VAMPIRE_CROSSBOW.get()).add(AAPItems.MAGIC_BEAN.get());
        this.tag(AetherTags.Items.GOLD_DUNGEON_LOOT).add(AAPItems.VAMPIRE_CROSSBOW.get());
    }
}