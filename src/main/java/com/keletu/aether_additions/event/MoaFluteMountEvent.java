package com.keletu.aether_additions.event;

import com.aetherteam.aether.entity.passive.Moa;
import com.keletu.aether_additions.AetherAdditions;
import com.keletu.aether_additions.client.MoaFluteWindAnimation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

@EventBusSubscriber(modid = AetherAdditions.MODID)
public final class MoaFluteMountEvent {
    private MoaFluteMountEvent() {
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!event.isMounting()) {
            return;
        }

        Entity mountedEntity = event.getEntityBeingMounted();

        if (!(mountedEntity instanceof Moa moa)) {
            return;
        }

        if (!MoaFluteWindAnimation.isAnimating(moa)) {
            return;
        }

        event.setCanceled(true);

        if (!event.getLevel().isClientSide() && event.getEntityMounting() instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("message.aether_additions.moa_flute.cannot_mount_in_wind"), true);
        }
    }
}