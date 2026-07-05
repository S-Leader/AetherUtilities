package com.keletu.aether_additions.client;

import com.keletu.aether_additions.AetherAdditions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AetherAdditions.MODID, value = Dist.CLIENT)
public final class AetherCrossbowClientEvents {
    private AetherCrossbowClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(AetherCrossbowItemProperties::register);
    }
}
