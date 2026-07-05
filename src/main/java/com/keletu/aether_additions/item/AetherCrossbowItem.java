package com.keletu.aether_additions.item;

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

/**
 * 1.21.1 NeoForge port of the old Aether crossbows.
 * <p>
 * Important behavior:
 * - This still extends vanilla CrossbowItem.
 * - It does NOT register or use a custom bolt/projectile entity.
 * - Ammo lookup/loading/firing is still vanilla CrossbowItem behavior.
 * - Special shots are applied by editing the vanilla CHARGED_PROJECTILES component after vanilla loading.
 * - No forced sneaking/crouching is required for charging.
 */
public class AetherCrossbowItem extends CrossbowItem {
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        int convertedTimeLeft = this.convertRemainingUseDurationForVanilla(stack, living, timeLeft);
        super.releaseUsing(stack, level, living, convertedTimeLeft);

        // After vanilla CrossbowItem has found and loaded ammo, expand the charged projectile list for special crossbows.
        // This keeps vanilla ammo compatibility instead of hardcoding minecraft:arrow or a custom bolt item.
        this.expandChargedProjectiles(stack);
    }

    private void expandChargedProjectiles(ItemStack weaponStack) {
        int projectileCount = this.type.extraProjectileCount;
        if (projectileCount <= 1) {
            return;
        }

        ChargedProjectiles chargedProjectiles = weaponStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) {
            return;
        }

        List<ItemStack> originalProjectiles = chargedProjectiles.getItems();
        if (originalProjectiles.isEmpty()) {
            return;
        }

        // If another vanilla mechanic/mod already loaded multiple projectiles, do not multiply it again.
        if (originalProjectiles.size() != 1) {
            return;
        }

        ItemStack originalProjectile = originalProjectiles.get(0);
        List<ItemStack> expandedProjectiles = new ArrayList<>(projectileCount);
        for (int i = 0; i < projectileCount; i++) {
            ItemStack copy = originalProjectile.copy();
            copy.setCount(1);
            expandedProjectiles.add(copy);
        }

        weaponStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(expandedProjectiles));
    }

    @Override
    public void performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weaponStack, float velocity, float inaccuracy, @Nullable LivingEntity target) {
        // Safety: if another path charged the weapon without releaseUsing being called, still apply the special list before firing.
        this.expandChargedProjectiles(weaponStack);
        super.performShooting(level, shooter, hand, weaponStack, velocity, inaccuracy, target);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int projectileIndex, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, projectileIndex, velocity, inaccuracy, angle, target);

        if (projectileIndex > 0 && projectile instanceof AbstractArrow arrow) {
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean isCrit) {
        Projectile projectile = super.createProjectile(level, shooter, weaponStack, projectileStack, isCrit);

        if (projectile instanceof AbstractArrow arrow) {
            if (this.type == CrossbowType.ZANITE && weaponStack.isDamageableItem() && weaponStack.getMaxDamage() > 0) {
                double bonusDamage = (weaponStack.getDamageValue() * 7.0D) / weaponStack.getMaxDamage();
                arrow.setBaseDamage(arrow.getBaseDamage() + bonusDamage);
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
}
