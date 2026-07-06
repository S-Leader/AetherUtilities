package com.keletu.aether_additions.data;

import com.aetherteam.aether.item.AetherCreativeTabs;
import com.aetherteam.aether.item.AetherItems;
import com.keletu.aether_additions.item.AetherCrossbowItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class AACreativeTabs {
    public static void buildCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();
        if (tab == AetherCreativeTabs.AETHER_EQUIPMENT_AND_UTILITIES.getKey()) {
            event.insertAfter(new ItemStack(AetherItems.ENCHANTED_DART_SHOOTER.get()), new ItemStack(AetherCrossbowItems.SKYROOT_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherCrossbowItems.SKYROOT_CROSSBOW.get()), new ItemStack(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherCrossbowItems.HOLYSTONE_CROSSBOW.get()), new ItemStack(AetherCrossbowItems.ZANITE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherCrossbowItems.ZANITE_CROSSBOW.get()), new ItemStack(AetherCrossbowItems.GRAVITITE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherItems.VAMPIRE_BLADE.get()), new ItemStack(AetherCrossbowItems.VAMPIRE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}