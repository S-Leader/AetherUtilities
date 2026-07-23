package com.keletu.aether_additions.item;

import com.aetherteam.aether.item.AetherItems;
import com.keletu.aether_additions.AetherAdditions;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AAPItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AetherAdditions.MODID);

    public static final DeferredItem<AetherCrossbowItem> SKYROOT_CROSSBOW = ITEMS.register("skyroot_crossbow",
            () -> new AetherCrossbowItem(new Item.Properties(), AetherCrossbowItem.CrossbowType.SKYROOT));

    public static final DeferredItem<AetherCrossbowItem> HOLYSTONE_CROSSBOW = ITEMS.register("holystone_crossbow",
            () -> new AetherCrossbowItem(new Item.Properties(), AetherCrossbowItem.CrossbowType.HOLYSTONE));

    public static final DeferredItem<AetherCrossbowItem> ZANITE_CROSSBOW = ITEMS.register("zanite_crossbow",
            () -> new AetherCrossbowItem(new Item.Properties(), AetherCrossbowItem.CrossbowType.ZANITE));

    public static final DeferredItem<AetherCrossbowItem> GRAVITITE_CROSSBOW = ITEMS.register("gravitite_crossbow",
            () -> new AetherCrossbowItem(new Item.Properties(), AetherCrossbowItem.CrossbowType.GRAVITITE));

    public static final DeferredItem<AetherCrossbowItem> VAMPIRE_CROSSBOW = ITEMS.register("vampire_crossbow",
            () -> new AetherCrossbowItem(new Item.Properties().rarity(AetherItems.AETHER_LOOT), AetherCrossbowItem.CrossbowType.VAMPIRE));

    public static final DeferredItem<MoaFluteItem> MOA_FLUTE = ITEMS.register("moa_flute",
            () -> new MoaFluteItem(new Item.Properties().stacksTo(1)));

    private AAPItems() {
    }
}
