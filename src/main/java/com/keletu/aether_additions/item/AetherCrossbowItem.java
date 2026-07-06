package com.keletu.aether_additions.item;

import com.aetherteam.aether.AetherTags;
import com.aetherteam.aether.item.EquipmentUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AetherCrossbowItem extends CrossbowItem {
    public static final String AETHER_CROSSBOW_ARROW_TAG = "aether_adds:aether_crossbow_arrow";
    public static final String VAMPIRE_CROSSBOW_ARROW_TAG = "aether_adds:vampire_crossbow_arrow";
    private final CrossbowType type;
    private final int durationInTicks;
    private final float knockBackValue;

    public AetherCrossbowItem(Properties properties, CrossbowType type) {
        super(properties.stacksTo(1).durability(type.maxDurability));
        this.type = type;
        this.durationInTicks = type.durationInTicks;
        this.knockBackValue = type.knockBackValue;
    }

    public CrossbowType getType() {
        return this.type;
    }

    public int getDurationInTicks() {
        return this.durationInTicks;
    }

    /**
     * Kept for compatibility with old code/config displays.
     * There is intentionally no setKnockback call here: that method is not available in 1.21.1.
     */
    public float getKnockBackValue() {
        return this.knockBackValue;
    }

    /**
     * CrossbowItem#getChargeDuration is static, so subclasses cannot override it.
     * We preserve vanilla Quick Charge behavior by taking vanilla's charge reduction and applying it to this crossbow's base duration.
     */
    public int getAetherChargeDuration(ItemStack stack, LivingEntity living) {
        int vanillaDuration = CrossbowItem.getChargeDuration(stack, living);
        int vanillaReduction = 25 - vanillaDuration;
        return Math.max(1, this.durationInTicks - vanillaReduction);
    }

    private int convertRemainingUseDurationForVanilla(ItemStack stack, LivingEntity living, int aetherRemainingUseDuration) {
        int vanillaUseDuration = super.getUseDuration(stack, living);

        int usedTicks = Mth.clamp(vanillaUseDuration - aetherRemainingUseDuration, 0, this.getAetherChargeDuration(stack, living));

        int aetherChargeDuration = this.getAetherChargeDuration(stack, living);
        int vanillaChargeDuration = CrossbowItem.getChargeDuration(stack, living);

        int vanillaUsedTicks = usedTicks >= aetherChargeDuration ? vanillaChargeDuration : Mth.floor((usedTicks / (float) aetherChargeDuration) * vanillaChargeDuration);


        return vanillaUseDuration - vanillaUsedTicks;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        int convertedRemaining = this.convertRemainingUseDurationForVanilla(stack, living, remainingUseDuration);
        super.onUseTick(level, living, stack, convertedRemaining);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return switch (this.type) {
            case SKYROOT ->
                    repairCandidate.getTags().anyMatch((tagKey) -> tagKey.equals(AetherTags.Items.SKYROOT_REPAIRING));
            case HOLYSTONE ->
                    repairCandidate.getTags().anyMatch((tagKey) -> tagKey.equals(AetherTags.Items.HOLY_REPAIRING));
            case ZANITE ->
                    repairCandidate.getTags().anyMatch((tagKey) -> tagKey.equals(AetherTags.Items.ZANITE_REPAIRING));
            case GRAVITITE ->
                    repairCandidate.getTags().anyMatch((tagKey) -> tagKey.equals(AetherTags.Items.GRAVITITE_REPAIRING));
            default -> false;
        };
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        int convertedTimeLeft = this.convertRemainingUseDurationForVanilla(stack, living, timeLeft);
        super.releaseUsing(stack, level, living, convertedTimeLeft);
    }


    @Override
    public void performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weaponStack, float velocity, float inaccuracy, @Nullable LivingEntity target) {
        ShotContext context = this.expandChargedProjectilesForShooting(weaponStack);

        SHOT_CONTEXT.set(context);
        try {
            super.performShooting(level, shooter, hand, weaponStack, velocity, inaccuracy, target);
        } finally {
            SHOT_CONTEXT.remove();
        }
    }

    private ShotContext expandChargedProjectilesForShooting(ItemStack weaponStack) {
        int aetherShotCount = this.type.extraProjectileCount;

        ChargedProjectiles chargedProjectiles = weaponStack.get(DataComponents.CHARGED_PROJECTILES);

        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) {
            return new ShotContext(aetherShotCount, 0);
        }

        List<ItemStack> originalProjectiles = chargedProjectiles.getItems();
        if (originalProjectiles.isEmpty()) {
            return new ShotContext(aetherShotCount, 0);
        }

        int vanillaShotCount = originalProjectiles.size();

        if (aetherShotCount <= 1) {
            return new ShotContext(aetherShotCount, vanillaShotCount);
        }

        List<ItemStack> expandedProjectiles = new ArrayList<>(vanillaShotCount * aetherShotCount);

        // 关键：复制“原版已装填弹丸组”
        // 如果没有散射，originalProjectiles 通常是 1 个。
        // 如果有散射，originalProjectiles 通常是 3 个。
        for (int aetherIndex = 0; aetherIndex < aetherShotCount; aetherIndex++) {
            for (ItemStack projectileStack : originalProjectiles) {
                ItemStack copy = projectileStack.copy();
                copy.setCount(1);
                expandedProjectiles.add(copy);
            }
        }

        weaponStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(expandedProjectiles));

        return new ShotContext(aetherShotCount, vanillaShotCount);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int projectileIndex, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        ShotContext context = SHOT_CONTEXT.get();

        float finalAngle = angle;

        if (context != null && context.vanillaShotCount > 0) {
            int vanillaIndex = projectileIndex % context.vanillaShotCount;
            int aetherIndex = projectileIndex / context.vanillaShotCount;

            float vanillaScatterAngle = this.getVanillaScatterAngle(vanillaIndex, context.vanillaShotCount);
            float aetherExtraAngle = this.getAetherExtraAngle(aetherIndex);

            finalAngle = vanillaScatterAngle + aetherExtraAngle;
        }

        super.shootProjectile(shooter, projectile, projectileIndex, velocity, inaccuracy, finalAngle, target);

        if (projectile instanceof AbstractArrow arrow) {
            if (context != null && context.isAetherExtraProjectile(projectileIndex)) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
        }
    }

    private float getVanillaScatterAngle(int vanillaIndex, int vanillaShotCount) {
        if (vanillaShotCount == 3) {
            return switch (vanillaIndex) {
                case 1 -> -10.0F;
                case 2 -> 10.0F;
                default -> 0.0F;
            };
        }

        return 0.0F;
    }

    private float getAetherExtraAngle(int aetherIndex) {
        if (this.type == CrossbowType.HOLYSTONE) {
            return switch (aetherIndex) {
                case 1 -> 10.0F;
                case 2 -> -10.0F;
                default -> 0.0F;
            };
        }

        return 0.0F;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean isCrit) {
        Projectile projectile = super.createProjectile(level, shooter, weaponStack, projectileStack, isCrit);
        projectile.getPersistentData().putBoolean(AETHER_CROSSBOW_ARROW_TAG, true);
        if (this.type == CrossbowType.VAMPIRE) {
            projectile.getPersistentData().putBoolean(VAMPIRE_CROSSBOW_ARROW_TAG, true);
        }
        if (projectile instanceof AbstractArrow arrow) {

            if (this.type == CrossbowType.ZANITE) {
                double baseDamage = arrow.getBaseDamage();
                double boostedDamage = EquipmentUtil.calculateZaniteBuff(weaponStack, baseDamage);

                arrow.setBaseDamage(boostedDamage);
            }

            if (this.type == CrossbowType.GRAVITITE) {
                arrow.setNoGravity(true);
            }
        }

        return projectile;
    }

    public enum CrossbowType {
        SKYROOT(20, 0.5F, 82, 2), HOLYSTONE(30, 0.7F, 181, 3), ZANITE(15, 0.5F, 346, 1), GRAVITITE(25, 1.2F, 2160, 1), VAMPIRE(20, 0.7F, 2160, 1);

        private final int durationInTicks;
        private final float knockBackValue;
        private final int maxDurability;
        private final int extraProjectileCount;

        CrossbowType(int durationInTicks, float knockBackValue, int maxDurability, int extraProjectileCount) {
            this.durationInTicks = durationInTicks;
            this.knockBackValue = knockBackValue;
            this.maxDurability = maxDurability;
            this.extraProjectileCount = extraProjectileCount;
        }

        public int durationInTicks() {
            return this.durationInTicks;
        }

        public float knockBackValue() {
            return this.knockBackValue;
        }

        public int maxDurability() {
            return this.maxDurability;
        }

        public int extraProjectileCount() {
            return this.extraProjectileCount;
        }
    }

    private static final ThreadLocal<ShotContext> SHOT_CONTEXT = new ThreadLocal<>();

    private record ShotContext(int aetherShotCount, int vanillaShotCount) {
        boolean hasAetherExtraShots() {
            return this.aetherShotCount > 1;
        }

        boolean isAetherExtraProjectile(int projectileIndex) {
            return projectileIndex >= this.vanillaShotCount;
        }
    }
}
