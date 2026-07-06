package com.keletu.aether_additions;

import com.keletu.aether_additions.item.AetherCrossbowItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = AetherAdditions.MODID)
public final class AetherCrossbowCombatEvents {
    private static final DustParticleOptions VAMPIRE_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F);

    private AetherCrossbowCombatEvents() {
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }

        if (!arrow.getPersistentData().getBoolean(AetherCrossbowItem.VAMPIRE_CROSSBOW_ARROW_TAG)) {
            return;
        }

        HitResult result = event.getRayTraceResult();
        if (!(result instanceof EntityHitResult entityHitResult)) {
            return;
        }

        Entity hitEntity = entityHitResult.getEntity();
        if (!(hitEntity instanceof LivingEntity target)) {
            return;
        }

        if (!(arrow.getOwner() instanceof LivingEntity shooter)) {
            return;
        }

        if (target == shooter || shooter.isDeadOrDying()) {
            return;
        }

        if (!(shooter.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        shooter.heal(1.0F);

        spawnVampireParticles(serverLevel, target, shooter);
    }

    private static void spawnVampireParticles(ServerLevel level, LivingEntity target, LivingEntity shooter) {
        Vec3 from = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 to = shooter.position().add(0.0D, shooter.getBbHeight() * 0.55D, 0.0D);
        Vec3 delta = to.subtract(from);

        double distance = delta.length();
        if (distance <= 0.01D) {
            return;
        }

        int steps = Mth.clamp((int) (distance * 6.0D), 8, 48);

        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;
            Vec3 pos = from.add(delta.scale(progress));

            level.sendParticles(
                    VAMPIRE_DUST,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0.015D,
                    0.015D,
                    0.015D,
                    0.0D
            );
        }
    }
}