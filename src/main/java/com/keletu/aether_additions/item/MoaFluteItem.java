package com.keletu.aether_additions.item;

import com.aetherteam.aether.api.registers.MoaType;
import com.aetherteam.aether.entity.AetherEntityTypes;
import com.aetherteam.aether.entity.passive.Moa;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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

    public MoaFluteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (!(target instanceof Moa moa)) {
            return InteractionResult.PASS;
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

        // Do not mutate only the interaction stack in place. Creative mode keeps a
        // client-side hotbar copy which can overwrite an in-place server mutation.
        ItemStack filledFlute = stack.copy();
        filledFlute.set(AAPDataComponents.MOA.get(), storedData.copy());
        replaceHeldStackAndSync(player, hand, filledFlute);

        level.playSound(null, moa.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.0F, 0.8F);
        player.swing(hand, true);
        moa.discard();
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
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
        moa.moveTo(x, y, z, yaw, 0.0F);

        // Do not delete the stored Moa when the target space is obstructed.
        if (!level.noCollision(moa)) {
            Player player = context.getPlayer();
            if (player != null) {
                showMessage(player, "message.aether_additions.moa_flute.blocked");
            }
            return InteractionResult.FAIL;
        }

        if (!level.addFreshEntity(moa)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        if (player != null) {
            ItemStack emptyFlute = stack.copy();
            emptyFlute.remove(AAPDataComponents.MOA.get());
            replaceHeldStackAndSync(player, context.getHand(), emptyFlute);
        } else {
            stack.remove(AAPDataComponents.MOA.get());
        }

        level.playSound(null, moa.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.NEUTRAL, 1.0F, 1.2F);
        if (player != null) {
            player.swing(context.getHand(), true);
        }
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