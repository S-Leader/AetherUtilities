package com.keletu.aether_additions.client;

import com.aetherteam.aether.entity.passive.Moa;
import com.keletu.aether_additions.AetherAdditions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = AetherAdditions.MODID)
public final class MoaFluteWindAnimation {
    private static final String PREFIX = "aether_additions:MoaFluteWind";

    private static final String MODE = PREFIX + "Mode";
    private static final String TICK = PREFIX + "Tick";

    private static final String BASE_X = PREFIX + "BaseX";
    private static final String BASE_Y = PREFIX + "BaseY";
    private static final String BASE_Z = PREFIX + "BaseZ";

    private static final String OLD_NO_AI = PREFIX + "OldNoAi";
    private static final String OLD_NO_GRAVITY = PREFIX + "OldNoGravity";
    private static final String OLD_INVULNERABLE = PREFIX + "OldInvulnerable";
    private static final String OLD_NO_PHYSICS = PREFIX + "OldNoPhysics";

    private static final int CAPTURE = 1;
    private static final int RELEASE = 2;

    public static final int ANIMATION_LENGTH = 32;

    private static final double WIND_HEIGHT = 2.75D;

    private MoaFluteWindAnimation() {
    }

    public static boolean isAnimating(Moa moa) {
        return moa.getPersistentData().contains(MODE, Tag.TAG_INT);
    }

    public static boolean startCapture(Moa moa) {
        if (moa.level().isClientSide() || isAnimating(moa)) {
            return false;
        }

        prepareAnimation(moa, CAPTURE, moa.position());

        if (moa.level() instanceof ServerLevel serverLevel) {
            spawnSmokeBurst(serverLevel, moa.position().add(0.0D, 0.6D, 0.0D), 10);
        }

        return true;
    }

    public static boolean startRelease(Moa moa, Vec3 targetPosition) {
        if (moa.level().isClientSide() || isAnimating(moa)) {
            return false;
        }

        prepareAnimation(moa, RELEASE, targetPosition);
        moa.moveTo(targetPosition.x, targetPosition.y + WIND_HEIGHT, targetPosition.z, moa.getYRot(), 0.0F);

        if (moa.level() instanceof ServerLevel serverLevel) {
            spawnReleaseSmoke(serverLevel, targetPosition);
        }

        return true;
    }

    private static void prepareAnimation(Moa moa, int mode, Vec3 basePosition) {
        CompoundTag data = moa.getPersistentData();

        data.putInt(MODE, mode);
        data.putInt(TICK, 0);

        data.putDouble(BASE_X, basePosition.x);
        data.putDouble(BASE_Y, basePosition.y);
        data.putDouble(BASE_Z, basePosition.z);

        // 保存恐鸟原本的实体状态，召回结束后恢复。
        data.putBoolean(OLD_NO_AI, moa.isNoAi());
        data.putBoolean(OLD_NO_GRAVITY, moa.isNoGravity());
        data.putBoolean(OLD_INVULNERABLE, moa.isInvulnerable());
        data.putBoolean(OLD_NO_PHYSICS, moa.noPhysics);

        moa.getNavigation().stop();
        moa.setTarget(null);

        moa.setNoAi(true);
        moa.setNoGravity(true);
        moa.setInvulnerable(true);
        moa.noPhysics = true;

        moa.setDeltaMovement(Vec3.ZERO);
        moa.fallDistance = 0.0F;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Moa moa)) {
            return;
        }

        if (!(moa.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        CompoundTag data = moa.getPersistentData();

        if (!data.contains(MODE, Tag.TAG_INT)) {
            return;
        }

        int mode = data.getInt(MODE);
        int animationTick = data.getInt(TICK) + 1;

        data.putInt(TICK, animationTick);

        if (mode != CAPTURE && mode != RELEASE) {
            restoreMoa(moa, data);
            clearAnimationData(data);
            return;
        }

        double baseX = data.getDouble(BASE_X);
        double baseY = data.getDouble(BASE_Y);
        double baseZ = data.getDouble(BASE_Z);

        double progress = Mth.clamp(animationTick / (double) ANIMATION_LENGTH, 0.0D, 1.0D);

        /*
         * 缓出曲线：
         * 开始移动明显，结束时逐渐减速。
         */
        double easedProgress = 1.0D - Math.pow(1.0D - progress, 3.0D);

        /*
         * 半径在动画开始和结束时为 0，
         * 中间阶段达到最大值，形成真正的螺旋。
         */
        double radius = 0.48D * Math.sin(progress * Math.PI);

        double angle = animationTick * 0.72D;

        if (mode == RELEASE) {
            angle = -angle;
        }

        double x = baseX + Math.cos(angle) * radius;
        double z = baseZ + Math.sin(angle) * radius;

        double y;

        if (mode == CAPTURE) {
            y = baseY + WIND_HEIGHT * easedProgress;
        } else {
            y = baseY + WIND_HEIGHT * (1.0D - easedProgress);
        }

        float rotationSpeed = mode == CAPTURE ? 34.0F : -34.0F;

        float yaw = Mth.wrapDegrees(moa.getYRot() + rotationSpeed);

        moa.moveTo(x, y, z, yaw, 0.0F);

        moa.setYHeadRot(yaw);
        moa.setDeltaMovement(Vec3.ZERO);
        moa.fallDistance = 0.0F;

        spawnWindParticles(serverLevel, moa, angle, progress);

        if (animationTick < ANIMATION_LENGTH) {
            return;
        }

        if (mode == CAPTURE) {
            finishCapture(serverLevel, moa);
        } else {
            finishRelease(serverLevel, moa, data, new Vec3(baseX, baseY, baseZ));
        }
    }

    private static void finishCapture(ServerLevel level, Moa moa) {
        spawnSmokeBurst(level, moa.position().add(0.0D, 0.7D, 0.0D), 28);

        clearAnimationData(moa.getPersistentData());

        /*
         * 笛子的物品数据已经在动画开始前保存，
         * 此处才真正移除世界中的恐鸟。
         */
        moa.discard();
    }

    private static void finishRelease(ServerLevel level, Moa moa, CompoundTag data, Vec3 targetPosition) {
        moa.moveTo(targetPosition.x, targetPosition.y, targetPosition.z, moa.getYRot(), 0.0F);

        restoreMoa(moa, data);
        clearAnimationData(data);

        moa.setDeltaMovement(Vec3.ZERO);
        moa.fallDistance = 0.0F;

        spawnSmokeBurst(level, targetPosition.add(0.0D, 0.6D, 0.0D), 12);
    }

    private static void restoreMoa(Moa moa, CompoundTag data) {
        moa.setNoAi(data.getBoolean(OLD_NO_AI));
        moa.setNoGravity(data.getBoolean(OLD_NO_GRAVITY));
        moa.setInvulnerable(data.getBoolean(OLD_INVULNERABLE));
        moa.noPhysics = data.getBoolean(OLD_NO_PHYSICS);

        moa.setDeltaMovement(Vec3.ZERO);
        moa.fallDistance = 0.0F;
    }

    private static void spawnWindParticles(ServerLevel level, Moa moa, double baseAngle, double progress) {
        /*
         * 四股云雾围绕恐鸟旋转。
         */
        for (int index = 0; index < 4; index++) {
            double angle = baseAngle + index * Math.PI * 0.5D;

            double radius = 0.48D + index * 0.035D;

            double particleX = moa.getX() + Math.cos(angle) * radius;

            double particleZ = moa.getZ() + Math.sin(angle) * radius;

            double particleY = moa.getY() + 0.15D + index * 0.34D;

            level.sendParticles(ParticleTypes.CLOUD, particleX, particleY, particleZ, 1, 0.025D, 0.025D, 0.025D, 0.015D);
        }

        /*
         * 中间增加少量更细的白烟，
         * 避免旋风看起来只有四个孤立粒子。
         */
        if (moa.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.POOF, moa.getX(), moa.getY() + 0.65D, moa.getZ(), 2, 0.22D, 0.45D, 0.22D, 0.01D);
        }

        /*
         * 接近消失或出现时逐渐增加烟雾。
         */
        if (progress > 0.75D) {
            level.sendParticles(ParticleTypes.CLOUD, moa.getX(), moa.getY() + 0.55D, moa.getZ(), 2, 0.20D, 0.35D, 0.20D, 0.01D);
        }
    }

    private static void spawnSmokeBurst(ServerLevel level, Vec3 position, int amount) {
        level.sendParticles(ParticleTypes.POOF, position.x, position.y, position.z, amount, 0.42D, 0.55D, 0.42D, 0.035D);

        level.sendParticles(ParticleTypes.CLOUD, position.x, position.y, position.z, Math.max(4, amount / 2), 0.32D, 0.45D, 0.32D, 0.02D);
    }

    private static void clearAnimationData(CompoundTag data) {
        data.remove(MODE);
        data.remove(TICK);

        data.remove(BASE_X);
        data.remove(BASE_Y);
        data.remove(BASE_Z);

        data.remove(OLD_NO_AI);
        data.remove(OLD_NO_GRAVITY);
        data.remove(OLD_INVULNERABLE);
        data.remove(OLD_NO_PHYSICS);
    }

    public static void cancelAnimation(Moa moa) {
        CompoundTag data = moa.getPersistentData();

        if (!data.contains(MODE, Tag.TAG_INT)) {
            return;
        }

        restoreMoa(moa, data);
        clearAnimationData(data);

        moa.setDeltaMovement(Vec3.ZERO);
        moa.fallDistance = 0.0F;
    }

    private static void spawnReleaseSmoke(ServerLevel level, Vec3 position) {
        for (int layer = 0; layer < 8; layer++) {
            double height = 0.15D + layer * 0.34D;

            double radius = 0.10D + layer * 0.04D;

            level.sendParticles(ParticleTypes.CLOUD, position.x, position.y + height, position.z, 4, radius, 0.07D, radius, 0.025D);
        }

        level.sendParticles(ParticleTypes.POOF, position.x, position.y + 0.25D, position.z, 18, 0.35D, 0.20D, 0.35D, 0.035D);
    }
}