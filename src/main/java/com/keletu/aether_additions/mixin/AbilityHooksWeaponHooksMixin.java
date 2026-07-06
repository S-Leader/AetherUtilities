package com.keletu.aether_additions.mixin;

import com.aetherteam.aether.event.hooks.AbilityHooks;
import com.keletu.aether_additions.item.AetherCrossbowItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbilityHooks.WeaponHooks.class, remap = false)
public abstract class AbilityHooksWeaponHooksMixin {

    @Inject(method = "reduceWeaponEffectiveness", at = @At("HEAD"), cancellable = true, remap = false)
    private static void whyAreYouHardCoded(LivingEntity target, Entity source, float damage, CallbackInfoReturnable<Float> cir) {
        if (source instanceof AbstractArrow arrow && arrow.getPersistentData().getBoolean(AetherCrossbowItem.AETHER_CROSSBOW_ARROW_TAG)) {
            cir.setReturnValue(damage);
        }
    }
}