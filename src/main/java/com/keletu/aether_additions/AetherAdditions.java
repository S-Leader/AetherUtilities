package com.keletu.aether_additions;

import com.keletu.aether_additions.item.AetherCrossbowItems;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(AetherAdditions.MODID)
public class AetherAdditions {
    public static final String MODID = "aether_additions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AetherAdditions(IEventBus modEventBus, ModContainer container) {
        AetherCrossbowItems.ITEMS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
        }
    }
}