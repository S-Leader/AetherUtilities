package com.keletu.aether_additions.data;

import com.aetherteam.aether.item.AetherCreativeTabs;
import com.aetherteam.aether.item.AetherItems;
import com.keletu.aether_additions.item.AAPItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class AACreativeTabs {
    public static void buildCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = event.getTabKey();
        if (tab == AetherCreativeTabs.AETHER_EQUIPMENT_AND_UTILITIES.getKey()) {
            event.insertAfter(new ItemStack(AetherItems.ENCHANTED_DART_SHOOTER.get()), new ItemStack(AAPItems.SKYROOT_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AAPItems.SKYROOT_CROSSBOW.get()), new ItemStack(AAPItems.HOLYSTONE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AAPItems.HOLYSTONE_CROSSBOW.get()), new ItemStack(AAPItems.ZANITE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AAPItems.ZANITE_CROSSBOW.get()), new ItemStack(AAPItems.GRAVITITE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherItems.VAMPIRE_BLADE.get()), new ItemStack(AAPItems.VAMPIRE_CROSSBOW.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(new ItemStack(AetherItems.NATURE_STAFF.get()), new ItemStack(AAPItems.MOA_FLUTE.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
