package com.keletu.aether_additions.item;

import com.aetherteam.aether.api.registers.MoaType;
import com.aetherteam.aether.entity.AetherEntityTypes;
import com.aetherteam.aether.entity.passive.Moa;
import com.keletu.aether_additions.client.MoaFluteWindAnimation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MoaFluteItem extends Item {
    private static final String ENTITY_TAG = "EntityTag";
    private static final String DISPLAY_NAME = "DisplayName";
    private static final String MOA_TYPE = "MoaType";
    private static final String STORED_BY = "StoredBy";
    private static final String FLUTE_OWNER = "aether_additions:MoaFluteOwner";
    private static final String CAPTURE_LOCK_UNTIL = "CaptureLockUntil";
    private static final String CAPTURE_ENTITY_UUID = "CaptureEntityUUID";
    private static final String CAPTURE_DIMENSION = "CaptureDimension";

    private static final int CAPTURE_LOCK_BUFFER = 4;
    private static final int CAPTURE_LOCK_TICKS = 36;

    public MoaFluteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!(target instanceof Moa moa)) {
            return InteractionResult.PASS;
        }
        if (MoaFluteWindAnimation.isAnimating(moa)) {
            showMessage(player, "message.aether_additions.moa_flute.busy");
            return InteractionResult.FAIL;
        }

        if (hasStoredMoa(stack)) {
            showMessage(player, "message.aether_additions.moa_flute.full");
            return InteractionResult.FAIL;
        }

        if (!moa.isPlayerGrown()) {
            showMessage(player, "message.aether_additions.moa_flute.not_tamed");
            return InteractionResult.FAIL;
        }

        if (!canPlayerStore(moa, player)) {
            showMessage(player, "message.aether_additions.moa_flute.not_owner");
            return InteractionResult.FAIL;
        }

        // Prevent storing a player or another entity together with the Moa.
        if (moa.isVehicle() || moa.isPassenger()) {
            showMessage(player, "message.aether_additions.moa_flute.has_passenger");
            return InteractionResult.FAIL;
        }

        Level level = player.level();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag entityTag = new CompoundTag();
        moa.saveWithoutId(entityTag);

        CompoundTag storedData = new CompoundTag();
        storedData.put(ENTITY_TAG, entityTag);
        storedData.putString(DISPLAY_NAME, moa.getName().getString());
        storedData.putUUID(STORED_BY, player.getUUID());

        ResourceKey<MoaType> moaType = moa.getMoaTypeKey();
        if (moaType != null) {
            storedData.putString(MOA_TYPE, moaType.location().toString());
        }

        storedData.putUUID(CAPTURE_ENTITY_UUID, moa.getUUID());

        storedData.putString(CAPTURE_DIMENSION, level.dimension().location().toString());

        storedData.putLong(CAPTURE_LOCK_UNTIL, getServerGameTime(level) + MoaFluteWindAnimation.ANIMATION_LENGTH + CAPTURE_LOCK_BUFFER);

        if (!MoaFluteWindAnimation.startCapture(moa)) {
            return InteractionResult.FAIL;
        }

        ItemStack filledFlute = stack.copy();
        filledFlute.set(AAPDataComponents.MOA.get(), storedData.copy());
        replaceHeldStackAndSync(player, hand, filledFlute);
        level.playSound(null, moa.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.0F, 0.8F);

        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag storedData = getStoredMoa(stack);

        if (storedData == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        /*
         * 恐鸟还在收入旋风中时，不允许释放。
         * 这里不使用 ItemCooldowns。
         */
        if (isCaptureLocked(level, storedData)) {
            if (player != null) {
                showMessage(player, "message.aether_additions.moa_flute.busy");
            }

            return InteractionResult.FAIL;
        }

        CompoundTag entityTag = storedData.getCompound(ENTITY_TAG).copy();

        Moa moa = AetherEntityTypes.MOA.get().create(level);

        if (moa == null) {
            return InteractionResult.FAIL;
        }

        moa.load(entityTag);

        double x = context.getClickedPos().getX() + 0.5D;
        double y = context.getClickedPos().getY() + 1.0D;
        double z = context.getClickedPos().getZ() + 0.5D;

        float yaw = 180.0F + context.getHorizontalDirection().toYRot();

        Vec3 releasePosition = new Vec3(x, y, z);

        /*
         * 先在最终落地点检查碰撞。
         */
        moa.moveTo(x, y, z, yaw, 0.0F);

        if (!level.noCollision(moa)) {
            if (player != null) {
                showMessage(player, "message.aether_additions.moa_flute.blocked");
            }

            return InteractionResult.FAIL;
        }

        /*
         * 必须在 addFreshEntity 之前启动释放动画。
         *
         * startRelease 会：
         * 1. 写入动画状态；
         * 2. 关闭重力、AI和碰撞；
         * 3. 将恐鸟移动到落点上方。
         *
         * 因此客户端收到生成包时，恐鸟已经位于空中，
         * 不会再从地面突然升上去。
         */
        if (!MoaFluteWindAnimation.startRelease(moa, releasePosition)) {
            return InteractionResult.FAIL;
        }

        if (!level.addFreshEntity(moa)) {
            MoaFluteWindAnimation.cancelAnimation(moa);
            return InteractionResult.FAIL;
        }

        /*
         * 只有恐鸟成功加入世界并启动动画后才能清空笛子。
         */
        if (player != null) {
            ItemStack emptyFlute = stack.copy();
            emptyFlute.remove(AAPDataComponents.MOA.get());

            replaceHeldStackAndSync(player, context.getHand(), emptyFlute);

            player.swing(context.getHand(), true);
        } else {
            stack.remove(AAPDataComponents.MOA.get());
        }

        level.playSound(null, moa.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.0F, 1.2F);

        return InteractionResult.SUCCESS;
    }

    /**
     * Claims an unclaimed player-grown Moa for the first player using the flute.
     * Existing Aether following/last-rider data is respected before doing so.
     */
    private static boolean canPlayerStore(Moa moa, Player player) {
        CompoundTag persistentData = moa.getPersistentData();
        UUID playerId = player.getUUID();

        if (persistentData.hasUUID(FLUTE_OWNER)) {
            return persistentData.getUUID(FLUTE_OWNER).equals(playerId);
        }

        UUID following = moa.getFollowing();
        if (following != null) {
            if (!following.equals(playerId)) {
                return false;
            }
            claimForPlayer(moa, persistentData, playerId);
            return true;
        }

        UUID lastRider = moa.getLastRider();
        if (lastRider != null) {
            if (!lastRider.equals(playerId)) {
                return false;
            }
            claimForPlayer(moa, persistentData, playerId);
            return true;
        }

        // A freshly raised Moa can have no rider/following UUID yet.
        claimForPlayer(moa, persistentData, playerId);
        return true;
    }

    private static void claimForPlayer(Moa moa, CompoundTag persistentData, UUID playerId) {
        if (!moa.level().isClientSide()) {
            persistentData.putUUID(FLUTE_OWNER, playerId);
        }
    }


    /**
     * Replaces the held stack instead of mutating it in place. This is important
     * for creative mode, whose client-side hotbar stack can otherwise overwrite
     * the server-side data-component change with the old empty flute.
     */
    private static void replaceHeldStackAndSync(Player player, InteractionHand hand, ItemStack replacement) {
        player.setItemInHand(hand, replacement);
        player.getInventory().setChanged();

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
            if (serverPlayer.inventoryMenu != serverPlayer.containerMenu) {
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void showMessage(Player player, String translationKey) {
        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    public static boolean hasStoredMoa(ItemStack stack) {
        return getStoredMoa(stack) != null;
    }

    @Nullable
    private static CompoundTag getStoredMoa(ItemStack stack) {
        CompoundTag data = stack.get(AAPDataComponents.MOA.get());
        if (data == null || !data.contains(ENTITY_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return data.copy();
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasStoredMoa(stack) || super.isFoil(stack);
    }

    private static boolean isCaptureLocked(Level currentLevel, CompoundTag storedData) {
        long currentTime = getServerGameTime(currentLevel);

        if (currentTime < storedData.getLong(CAPTURE_LOCK_UNTIL)) {
            return true;
        }

        if (!storedData.hasUUID(CAPTURE_ENTITY_UUID)) {
            return false;
        }

        String dimensionName = storedData.getString(CAPTURE_DIMENSION);

        ResourceLocation dimensionId = ResourceLocation.tryParse(dimensionName);

        if (dimensionId == null || currentLevel.getServer() == null) {
            return false;
        }

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel sourceLevel = currentLevel.getServer().getLevel(dimensionKey);
        if (sourceLevel == null) {
            return false;
        }

        Entity sourceEntity = sourceLevel.getEntity(storedData.getUUID(CAPTURE_ENTITY_UUID));

        return sourceEntity instanceof Moa sourceMoa && MoaFluteWindAnimation.isAnimating(sourceMoa);
    }

    private static long getServerGameTime(Level level) {
        if (level.getServer() != null) {
            return level.getServer().overworld().getGameTime();
        }

        return level.getGameTime();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag data = getStoredMoa(stack);
        if (data == null) {
            tooltip.add(Component.translatable("tooltip.aether_additions.moa_flute.empty").withStyle(ChatFormatting.GRAY));
            return;
        }

        String name = data.getString(DISPLAY_NAME);
        if (name.isEmpty()) {
            name = Component.translatable(AetherEntityTypes.MOA.get().getDescriptionId()).getString();
        }
        tooltip.add(Component.translatable("tooltip.aether_additions.moa_flute.contains", name).withStyle(ChatFormatting.AQUA));

        String moaType = data.getString(MOA_TYPE);
        if (!moaType.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.aether_additions.moa_flute.type", moaType).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}